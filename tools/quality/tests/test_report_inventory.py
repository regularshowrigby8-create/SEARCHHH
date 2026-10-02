import unittest
from tools.quality.report_inventory import lint_records, detekt_records, format_records, export


class ReportInventoryTest(unittest.TestCase):
    def test_lint_secondary_locations_are_preserved_not_counted_as_extra_issues(self):
        records = lint_records('<issues><issue id="NewApi" severity="Error" message="guard API">'
                               '<location file="app/First.kt" line="4"/>'
                               '<location file="app/Second.kt" line="9" message="related"/>'
                               '</issue></issues>')
        self.assertEqual(len(records), 1)
        self.assertEqual(len(records[0]['locations']), 2)
        self.assertEqual(records[0]['locations'][1]['message'], 'related')

    def test_lint_variants_and_same_rule_occurrences_are_not_deduplicated(self):
        text = '<issue id="Typos" severity="Error" message="correct spelling"><location file="a.xml"/></issue>'
        records = lint_records('<issues>' + text * 2 + '</issues>')
        self.assertEqual(len(records), 2)

    def test_unknown_or_incomplete_lint_is_rejected(self):
        for text in ['<results/>', '<issues><issue id="NewApi"/></issues>']:
            with self.assertRaises(ValueError):
                lint_records(text)

    def test_detekt_retains_source_severity_and_normalizes_known_runner_prefix(self):
        text = ('<checkstyle><file name="/home/runner/work/SEARCHHH/SEARCHHH/app/A.kt">'
                '<error line="3" column="4" severity="warning" source="detekt.MagicNumber" '
                'message="Name the value"/></file></checkstyle>')
        records = detekt_records(text)
        self.assertEqual(records[0]['locations'][0]['file'], 'app/A.kt')
        self.assertEqual(records[0]['severity'], 'warning')
        self.assertEqual(records[0]['rule'], 'detekt.MagicNumber')

    def test_colored_formatting_reconciles_actual_footer(self):
        text = ('\x1b[90m/home/runner/work/SEARCHHH/SEARCHHH/app/\x1b[0mA.kt:3:5: Missing newline '
                '(standard:statement-wrapping)\n\nSummary error count (descending) by rule:\n'
                '  standard:statement-wrapping: 1\n')
        records = format_records(text)
        self.assertEqual(len(records), 1)
        self.assertEqual(records[0]['locations'][0]['file'], 'app/A.kt')
        self.assertEqual(records[0]['rule'], 'standard:statement-wrapping')

    def test_formatting_never_silently_drops_unknown_lines_or_count_mismatches(self):
        valid = ('app/A.kt:1:1: Missing newline (standard:final-newline)\n'
                 'Summary error count (descending) by rule:\n  standard:final-newline: 1\n')
        for text in [valid.replace(': 1\n', ': 2\n'), valid + 'unparsed message\n',
                     valid.split('Summary')[0], valid + '  standard:final-newline: 1\n', '']:
            with self.subTest(text=text), self.assertRaises(ValueError):
                format_records(text)

    def test_missing_bundle_cannot_produce_complete_inventory(self):
        with self.assertRaisesRegex(ValueError, 'Missing report group'):
            export({})
