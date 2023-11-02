#!/bin/bash
#================================================================
# HEADER
#================================================================
#% SYNOPSIS
#+    escape_newlines.sh "<string>"
#%
#% DESCRIPTION
#%    This script converts newline characters in a string to
#%    the literal string '\n'. This is useful when you want to
#%    preserve the multiline format of a string when passing it
#%    as a single-line string to another process.
#%
#% EXAMPLES
#%    ./escape_newlines.sh "Line1
#%    Line2"
#================================================================
# END_OF_HEADER
#================================================================

input="$1"  # Capture the argument passed to this script

# Use sed to replace newline characters with the string '\n'
echo "$input" | sed ':a;N;$!ba;s/\n/\\n/g'
