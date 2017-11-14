#!/bin/bash
export SWT_GTK3=0
export LIBOVERLAY_SCROLLBAR=0
cd "`dirname $0`"
java -jar lib/ratel.jar $*
