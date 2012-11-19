#
# This file is part of cljmex.
#
# Copyright (C) 2012, Stefan Sommer (sommer@diku.dk)
# https://github.com/nefan/cljmex
#
# cljmex is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# cljmex is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with cljmex.  If not, see <http://www.gnu.org/licenses/>.
#

.SUFFIXES: .mexa64 .cpp

# clojure command (on Ubuntu, install clojure and leiningen with 'apt-get install clojure1.4 leiningen')
CLJ=clojure
# mex
MEX=mex

# source files
MEXSOURCES=mexfile.cpp
MEXHEADERS=$(MEXSOURCES:.cpp=.hpp)
MEXOBJECTS=$(MEXSOURCES:.cpp=.mexa64)
MEXFLAGS=-largeArrayDims # matlab sparse stuff
CFLAGS+=-fPIC

# cljmex paths
CLJMEXHOME=..
CLJMEXSRC=$(CLJMEXHOME)/src
CLJMEXINCLUDE=$(CLJMEXHOME)/include

CLJMEXHEADERS=$(MEXSOURCES:.cpp=.cljmex.hpp)

# include path
INCLUDEPATH+=$(CLJMEXINCLUDE)
INC=$(foreach d, $(INCLUDEPATH), -I'$d')

# turn on MEX debugging
CFLAGS+=-DNDEBUG

CXXFLAGS+=$(CFLAGS)

#-----------------------------------------------------------------------------
# Make rules:
#-----------------------------------------------------------------------------
all: $(MEXOBJECTS)

$(MEXOBJECTS): $(MEXSOURCES) $(MEXHEADERS) $(CLJMEXHEADERS) Makefile

# cljmex rule
$(CLJMEXHEADERS): %.cljmex.hpp: %.cljmex
	clojure -cp $(CLJMEXSRC) $*.cljmex > $@

$(MEXOBJECTS): %.mexa64:
	$(MEX) $(MEXFLAGS) CFLAGS='$$CFLAGS $(CFLAGS)' CXXFLAGS='$$CXXFLAGS $(CXXFLAGS)' \
		LDFLAGS='$$LDFLAGS $(LDFLAGS)' CXXLIBS='$$CXXLIBS $(CXXLIBS)' $(INC) $*.cpp

clean:
	rm -f ${MEXOBJECTS} $(CLJMEXHEADERS)
