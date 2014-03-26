// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//------------------------------------------------------------------------
#include "StdAfx.h"
#include "base64.h"

#include <sstream>
#include <streambuf>
#include "..\TextHelpers.h"

#ifdef UNITTEST_DEPENDENCY
#include "..\UnitTest\UnitTest.h"
IMPLEMENT_MODULE(Base64)
#endif //ifdef UNITTEST_DEPENDENCY

#ifdef COMPILE_MODULE_INFO
#include "..\timestamp.h"
#ifndef MODULE_TIME_STAMP_USED
#define MODULE_TIME_STAMP_USED
MODULE_TIME_STAMP
#endif
#endif //ifdef COMPILE_MODULE_INFO

//------------------------------------------------------

const int _base64Chars[]=
{
	'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
	'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
	'0','1','2','3','4','5','6','7','8','9',
	'+','/'
};

//------------------------------------------------------

std::string base64_encode(const std::string& s, const int nMaxLineLength)
{
	int _State = 0;
	base64<char> encoder;
	std::ostringstream stream;
	std::ostreambuf_iterator<char> _Out(stream);
	encoder.put(s.begin(), s.end(), _Out, _State, base64<>::crlf(), nMaxLineLength);
	return stream.str();
}

//------------------------------------------------------

inline size_t PadTo4(size_t x)
{
	return 3 - (x-1) % 4;
}

//------------------------------------------------------

std::string repair_base64(const std::string& s)	// 'AB' -> 'AB==', 'ABC' -> 'ABC=', ...
{
	std::string norm = s;
	findandreplace(norm, "\r\n", "");
	findandreplace(norm, "\r", "");
	findandreplace(norm, "\n", "");
	const size_t len = norm.length();
	const std::string sAdd(PadTo4(len), '=');	// base64 code consists of 4-chars groups that encode 3 bytes each.
	return s + sAdd;
}

//------------------------------------------------------

std::string base64_decode(const std::string& s)
{
	int _State = 0;
	base64<char> decoder;
	std::ostringstream stream;
	std::ostreambuf_iterator<char> _Out(stream);
	decoder.get(s.begin(), s.end(), _Out, _State);
	return stream.str();
}

//------------------------------------------------------
