import datetime
import tempfile
from pathlib import Path
import unittest
from tools.quality.unfinished import apply_exceptions, scan_text, source_files


class UnfinishedTests(unittest.TestCase):
    def rules(self, text, path='Sample.kt'):
        return {f.rule for f in scan_text(path, text)}

    def test_markers_in_comments_and_implementation_calls(self):
        self.assertIn('unfinished-marker', self.rules('// TODO implement\nfun load() = 1'))
        self.assertIn('unfinished-implementation', self.rules('fun load() = TODO("later")'))

    def test_strings_are_not_executable_code(self):
        self.assertEqual(set(), self.rules('val sample = "onClick = {} // TODO"'))

    def test_empty_comment_only_and_parameter_callbacks_fail(self):
        for body in ('', ' /* deliberately blank */ ', 'value -> ', '_ -> return@onClick'):
            with self.subTest(body=body):
                self.assertIn('empty-action', self.rules('Button(onClick = {' + body + '})'))

    def test_real_action_and_guard_are_not_stubs(self):
        self.assertEqual(set(), self.rules('Button(onClick = { model.search() })'))
        self.assertEqual(set(), self.rules('fun find(): Any? { if (cache.isEmpty()) return null; return cache.first() }'))

    def test_nested_braces_and_strings_do_not_hide_empty_callback(self):
        self.assertIn('empty-action', self.rules('if (ok) { val s = "}"; Button(onClick = { /* } */ }) }'))

    def test_silent_catch_fails_logging_catch_passes(self):
        self.assertIn('silent-catch', self.rules('try { load() } catch (e: Exception) { // ignored\n }'))
        self.assertEqual(set(), self.rules('try { load() } catch (e: Exception) { Log.e("Load", "failed", e) }'))

    def test_expression_and_block_stubs_fail(self):
        for text in ('fun load(): List<Item> = emptyList()\n', 'fun load(): Item? { return null }'):
            self.assertIn('constant-return-review', self.rules(text))

    def test_empty_function_and_string_returns_require_review(self):
        self.assertIn('empty-function-review', self.rules('fun invoke() { /* no behavior */ }'))
        self.assertIn('constant-return-review', self.rules('fun load(): String { return "" }'))
        self.assertIn('constant-return-review', self.rules('fun load(): String = ""\n'))
        self.assertIn('constant-return-review', self.rules('fun invoke() = Unit\n'))

    def test_python_empty_collection_stubs_require_review(self):
        for value in ['[]', '{}', '()', '""']:
            with self.subTest(value=value):
                self.assertIn('constant-return-review', self.rules('def load():\n    return ' + value + '\n', 'a.py'))

    def test_python_pass_and_catch_fail(self):
        self.assertIn('constant-return-review', self.rules('def load():\n    pass\n', 'a.py'))
        self.assertIn('silent-catch', self.rules('try:\n    load()\nexcept Exception:\n    pass\n', 'a.py'))

    def test_disabled_tests_fail(self):
        self.assertIn('disabled-test', self.rules('@Ignore\n@Test fun search() { check(true) }'))

    def test_generated_output_excluded_but_test_and_nested_source_build_scanned(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            names = ['app/build/Generated.kt', 'adblock-client/.cxx/debug/compiler.cpp',
                     'app/.externalNativeBuild/generated.cpp', 'app/src/test/Test.kt',
                     'app/src/main/build/Real.kt', 'app/src/main/.cxx/Real.kt']
            for name in names:
                file = root / name
                file.parent.mkdir(parents=True, exist_ok=True)
                file.write_text('fun value() = 1')
            self.assertEqual(set(names[3:]), {p.relative_to(root).as_posix() for p in source_files(root)})

    def test_exceptions_exact_expiring_and_test_backed(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            (root / 'regression.py').write_text('assert 1 == 1')
            findings = scan_text('Sample.kt', 'fun disabled(): Boolean = false\n')
            entry = dict(file='Sample.kt', rule=findings[0].rule, fingerprint=findings[0].fingerprint,
                         kind='legitimate', reason='Explicitly documented interface contract returning unsupported.',
                         owner='maintainer', expires='2026-10-01', regression_test='regression.py')
            today = datetime.date(2026, 9, 29)
            self.assertEqual(([], []), apply_exceptions(findings, [entry], root, today))
            self.assertTrue(apply_exceptions([], [entry], root, today)[1])
            self.assertTrue(apply_exceptions(findings, [entry, entry], root, today)[1])
            self.assertTrue(apply_exceptions(findings, [entry], root, datetime.date(2026, 10, 2))[1])
            self.assertTrue(apply_exceptions(findings, [{**entry, 'fingerprint': 'wrong'}], root, today)[1])

    def test_syntax_error_is_not_a_pass(self):
        self.assertIn('python-syntax-error', self.rules('def broken(', 'a.py'))


if __name__ == '__main__':
    unittest.main()
