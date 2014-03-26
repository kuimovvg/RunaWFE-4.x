// ComBase.cpp: implementation of the CComBase class.
// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "ComBase.h"

#include <rpc.h>
#pragma comment(lib, "Rpcrt4.lib")

#include <atlbase.h>
#ifndef ASSERT
#define ASSERT ATLASSERT
#endif
#ifndef TRACE
#define TRACE ATLTRACE
#endif

#ifdef COMPILE_MODULE_INFO
#include "timestamp.h"
#ifndef MODULE_TIME_STAMP_USED
#define MODULE_TIME_STAMP_USED
MODULE_TIME_STAMP
#endif
#endif //ifdef COMPILE_MODULE_INFO

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

CComBase::CComBase()
{
	m_RefCnt = 0;
}

CComBase::~CComBase()
{
	ASSERT(0 == m_RefCnt);
}

ULONG STDMETHODCALLTYPE  CComBase::AddRef()
{
	const LONG res = InterlockedIncrement(&m_RefCnt);
	return res;
}

ULONG STDMETHODCALLTYPE  CComBase::Release()
{
	const LONG res = InterlockedDecrement(&m_RefCnt);
	if(0 == res)
	{
		delete this;
	}
	return res;
}

HRESULT STDMETHODCALLTYPE CComBase::QueryInterface(
						 /* [in] */ REFIID riid,
						 /* [iid_is][out] */ void **ppvObject)
{
	if(NULL == ppvObject)
	{
		return E_INVALIDARG;
	}

	if(IID_IUnknown == riid)
	{
		*ppvObject = this;
		AddRef();
		return S_OK;
	}

#ifdef _DEBUG

#if _MSC_VER <= 1200
#ifdef _UNICODE
	typedef unsigned short* RPC_TSTR;
#else
	typedef unsigned char* RPC_TSTR;
#endif
#else
#ifdef _UNICODE
	typedef RPC_WSTR RPC_TSTR;
#else
	typedef RPC_CSTR RPC_TSTR;
#endif
#endif

	RPC_TSTR pStr = NULL;
	GUID guid = riid;
	const RPC_STATUS status = UuidToString( &guid, &pStr );
	ASSERT( sizeof(pStr[0]) == sizeof(TCHAR) );
	LPCTSTR szBuffer = reinterpret_cast<LPCTSTR>(pStr);

	TRACE( _T("CComBase::QueryInterface: invalid interface {%s}\n"), szBuffer );

	if( status == RPC_S_OK )
	{
		RpcStringFree( &pStr );
	}
#endif

	return E_NOINTERFACE;
}
