import os
import tempfile
os.environ.setdefault('DATABASE_URL','sqlite:///'+tempfile.mktemp(prefix='searchhh-test-',suffix='.db'))
os.environ.setdefault('SEARCHHH_ACCESS_TOKEN','test-only-not-a-real-secret-'+('x'*32))
