// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//------------------------------------------------------

#pragma once

#ifndef TSTRING_DEFINED
#	define TSTRING_DEFINED
#	ifdef _UNICODE
typedef std::wstring tstring;
#	else
typedef std::string tstring;
#	endif
#endif
