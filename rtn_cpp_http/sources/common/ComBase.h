// ComBase.h: interface for the CComBase class.
// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//////////////////////////////////////////////////////////////////////

#if !defined(COMBASE_H)
#define COMBASE_H

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <atlbase.h>

class CComBase: public IUnknown
{
public:
	CComBase();
	virtual ~CComBase();

	virtual ULONG STDMETHODCALLTYPE AddRef();
	virtual ULONG STDMETHODCALLTYPE Release();

	virtual HRESULT STDMETHODCALLTYPE QueryInterface(
		/* [in] */ REFIID riid,
		/* [iid_is][out] */ void **ppvObject);

	LONG GetRefCounter() const
	{
		return m_RefCnt;
	}

	template<class T>
	static void CreateObject(CComPtr<T>& ptr)
	{
		ptr = new T();
	}

private:
	LONG	m_RefCnt;
};

#endif // !defined(COMBASE_H)
