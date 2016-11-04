import re

for test_string in ['555-1212', 'ILL-EGAL']:
  if re.match(r'^\d{3}-\d{4}$', test_string):
    passed = test_string
  else:
    failed = test_string