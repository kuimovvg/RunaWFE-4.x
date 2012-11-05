// Tracing.h: interface for the Tracing class.
// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//------------------------------------------------------

#if !defined(AFX_TRACING_H__6CEC74B2_A13D_4C35_B081_DD1D289E4BBD__INCLUDED_)
#define AFX_TRACING_H__6CEC74B2_A13D_4C35_B081_DD1D289E4BBD__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <string>

//------------------------------------------------------

//#define CRYPT_TRACE_USER_TEXT
//#define CRYPT_TRACE_SOURCES_INFO

#if defined(CRYPT_TRACE_USER_TEXT) || defined(CRYPT_TRACE_SOURCES_INFO)
#include "encode\crypt_macro2.h"
#endif

//------------------------------------------------------

#ifdef COMPILE_MODULE_INFO
#include "timestamp.h"
#ifndef MODULE_TIME_STAMP_USED
#define MODULE_TIME_STAMP_USED
MODULE_TIME_STAMP
#endif
#endif //ifdef COMPILE_MODULE_INFO

//------------------------------------------------------

//#define USE_FUNCTION_NAME
#define USE_SPECIFIC_PROCESSINFO

//------------------------------------------------------

#ifdef USE_FUNCTION_NAME
#	ifdef CRYPT_TRACE_SOURCES_INFO
#		define __MY_FUNCTION__ STR_CRYPT2(__FUNCTION__)
#	else
#		define __MY_FUNCTION__ __FUNCTION__
#	endif
#else
#	ifdef CRYPT_TRACE_SOURCES_INFO
#		define __MY_FUNCTION__ NULL
#	else
#		define __MY_FUNCTION__ NULL
#	endif
#endif

//------------------------------------------------------

#if !defined(CRYPT_TRACE_USER_TEXT) && defined(CRYPT_TRACE_SOURCES_INFO)
#error Unsupported tracing macro set!
#endif
#if defined(CRYPT_TRACE_USER_TEXT) && !defined(CRYPT_TRACE_SOURCES_INFO)
#error Unsupported tracing macro set!
#endif

//------------------------------------------------------

#if defined(CRYPT_TRACE_USER_TEXT) && defined(CRYPT_TRACE_SOURCES_INFO)

#define _MYASSERT(reason, expr, exprString)	CTracer(reason, STR_CRYPT2(__FILE__), __LINE__, __MY_FUNCTION__).Assert(true, expr, STR_CRYPT2(exprString))
#define _MYASSERT2(reason, expr, exprString, timeout, defButton)	CTracer(reason, STR_CRYPT2(__FILE__), __LINE__, __MY_FUNCTION__).Assert(true, expr, STR_CRYPT2(exprString), timeout, defButton)

#define MYTRACE(fmt, ...)						CTracer(CTracer::REASON_TRACE, STR_CRYPT2(__FILE__), __LINE__, __MY_FUNCTION__).Trace(true, STR_CRYPT2(fmt), __VA_ARGS__)
#define MYASSERT(expr)							_MYASSERT(CTracer::REASON_ASSERT, expr, #expr)
#define MYASSERT2(expr, timeout, defButton)		_MYASSERT2(CTracer::REASON_ASSERT, expr, #expr, timeout, defButton)
#define MYWARNING(expr)							_MYASSERT(CTracer::REASON_WARNING, expr, #expr)
#define MYDUMP(str)								CTracer(CTracer::REASON_DUMP, STR_CRYPT2(__FILE__), __LINE__, __MY_FUNCTION__).MakeDump(true, STR_CRYPT2(str))
#define MYDUMP_RAW(str)							CTracer(CTracer::REASON_DUMP, STR_CRYPT2(__FILE__), __LINE__, __MY_FUNCTION__).MakeDump(false, str)

#define CHECK_EQUAL_ERR(expected, actual)		CTracer(CTracer::REASON_ASSERT, STR_CRYPT2(__FILE__), __LINE__, __MY_FUNCTION__).CheckEqual(expected, actual)
#define CHECK_EQUAL_WARN(expected, actual)		CTracer(CTracer::REASON_WARNING, STR_CRYPT2(__FILE__), __LINE__, __MY_FUNCTION__).CheckEqual(expected, actual)

#define CHECK_NOT_EQUAL_ERR(expected, actual)	CTracer(CTracer::REASON_ASSERT, STR_CRYPT2(__FILE__), __LINE__, __MY_FUNCTION__).CheckNotEqual(expected, actual)
#define CHECK_NOT_EQUAL_WARN(expected, actual)	CTracer(CTracer::REASON_WARNING, STR_CRYPT2(__FILE__), __LINE__, __MY_FUNCTION__).CheckNotEqual(expected, actual)

#endif

//------------------------------------------------------

#if !defined(CRYPT_TRACE_USER_TEXT) && !defined(CRYPT_TRACE_SOURCES_INFO)

#define _MYASSERT(reason, expr, exprString)	CTracer(reason, __FILE__, __LINE__, __MY_FUNCTION__).Assert(false, expr, exprString)
#define _MYASSERT2(reason, expr, exprString, timeout, defButton)	CTracer(reason, __FILE__, __LINE__, __MY_FUNCTION__).Assert(false, expr, exprString, timeout, defButton)

#define MYTRACE(fmt, ...)						CTracer(CTracer::REASON_TRACE, __FILE__, __LINE__, __MY_FUNCTION__).Trace(false, fmt, __VA_ARGS__)
#define MYASSERT(expr)							_MYASSERT(CTracer::REASON_ASSERT, expr, #expr)
#define MYASSERT2(expr, timeout, defButton)		_MYASSERT2(CTracer::REASON_ASSERT, expr, #expr, timeout, defButton)
#define MYWARNING(expr)							_MYASSERT(CTracer::REASON_WARNING, expr, #expr)
#define MYDUMP(str)								CTracer(CTracer::REASON_DUMP, __FILE__, __LINE__, __MY_FUNCTION__).MakeDump(false, str)
#define MYDUMP_RAW(str)							CTracer(CTracer::REASON_DUMP, __FILE__, __LINE__, __MY_FUNCTION__).MakeDump(false, str)

#define CHECK_EQUAL_ERR(expected, actual)		CTracer(CTracer::REASON_ASSERT, __FILE__, __LINE__, __MY_FUNCTION__).CheckEqual(expected, actual)
#define CHECK_EQUAL_WARN(expected, actual)		CTracer(CTracer::REASON_WARNING, __FILE__, __LINE__, __MY_FUNCTION__).CheckEqual(expected, actual)

#define CHECK_NOT_EQUAL_ERR(expected, actual)	CTracer(CTracer::REASON_ASSERT, __FILE__, __LINE__, __MY_FUNCTION__).CheckNotEqual(expected, actual)
#define CHECK_NOT_EQUAL_WARN(expected, actual)	CTracer(CTracer::REASON_WARNING, __FILE__, __LINE__, __MY_FUNCTION__).CheckNotEqual(expected, actual)

#endif

//------------------------------------------------------

class CTracer
{
public:
	enum TRACE_REASON
	{
		REASON_ASSERT,
		REASON_WARNING,
		REASON_TRACE,
		REASON_DUMP,
	};

// #ifdef CRYPT_TRACE_SOURCES_INFO
// 	typedef const std::string& ConstStringRef1;
// 	typedef std::string String1;
// #else
	typedef const char* ConstStringRef1;
	typedef const char* String1;
// #endif

	inline CTracer(
		const TRACE_REASON reason,
		ConstStringRef1	szFile,
		const int		nLine,
		ConstStringRef1	szFunction)
	{
		m_reason = reason;
		m_szFile = szFile;
		m_nLine = nLine;
		m_szFunction = szFunction;
	}

	void Trace(bool bCrypted, const char *format, ...);
	bool Assert(bool bCrypted, bool bExpression, LPCSTR szExpression, DWORD dwAssertTimeoutSec = 0, UINT nDefaultButton = 0);
	void MakeDump(bool bCrypted, LPCSTR szExpression);

	bool CheckEqual(const std::string& sExpected, const std::string& sActual);
	bool CheckEqual(int Expected, int Actual);

	bool CheckNotEqual(const std::string& sExpected, const std::string& sActual);
	bool CheckNotEqual(int Expected, int Actual);

	static inline void SetSpecificProcessInfo(const std::string sInfo)
	{
#ifdef USE_SPECIFIC_PROCESSINFO
		g_sSpecificProcessInfo = sInfo;
#endif
	}

	static inline std::string GetSpecificProcessInfo()
	{
#ifdef USE_SPECIFIC_PROCESSINFO
		return g_sSpecificProcessInfo;
#else
		return "";
#endif
	}

	static std::string FormatReason(const TRACE_REASON reason);

protected:
	void TraceInternal (const LPCSTR szMessage, const bool bCompilerLogCompliant,
						const DWORD dwAssertTimeoutSec, const UINT nDefaultButton);

	TRACE_REASON	m_reason;
	String1			m_szFile;
	int				m_nLine;
	String1			m_szFunction;

#ifdef USE_SPECIFIC_PROCESSINFO
	static std::string g_sSpecificProcessInfo;
#endif
};

//------------------------------------------------------

#endif // !defined(AFX_TRACING_H__6CEC74B2_A13D_4C35_B081_DD1D289E4BBD__INCLUDED_)
