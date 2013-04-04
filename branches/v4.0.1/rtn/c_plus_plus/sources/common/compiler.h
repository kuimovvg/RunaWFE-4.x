// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//------------------------------------------------------
#pragma once

//------------------------------------------------------

#if 0
#define MYVIRTUAL				virtual
#else
#define MYVIRTUAL
#endif

//------------------------------------------------------

// The following table describes the preferred macros in use by the Windows header files.
// 
// Minimum system required Macros to define 
// Windows Server 2008 NTDDI_VERSION >= NTDDI_LONGHORN 
// Windows Vista NTDDI_VERSION >= NTDDI_VISTA 
// Windows Server 2003 SP1 NTDDI_VERSION >= NTDDI_WS03SP1 
// Windows Server 2003 NTDDI_VERSION >= NTDDI_WS03 
// Windows XP SP2 NTDDI_VERSION >= NTDDI_WINXPSP2 
// Windows XP SP1 NTDDI_VERSION >= NTDDI_WINXPSP1 
// Windows XP NTDDI_VERSION >= NTDDI_WINXP 
// Windows 2000 SP4 NTDDI_VERSION >= NTDDI_WIN2KSP4 
// Windows 2000 SP3 NTDDI_VERSION >= NTDDI_WIN2KSP3 
// Windows 2000 SP2 NTDDI_VERSION >= NTDDI_WIN2KSP2 
// Windows 2000 SP1 NTDDI_VERSION >= NTDDI_WIN2KSP1 
// Windows 2000 NTDDI_VERSION >= NTDDI_WIN2K 
// 
// The following table describes the legacy macros in use by the Windows header files.
// 
// Minimum system required Macros to define 
// Windows Server 2008 _WIN32_WINNT>=0x0600
// WINVER>=0x0600
// 
// Windows Vista _WIN32_WINNT>=0x0600
// WINVER>=0x0600
// 
// Windows Server 2003 _WIN32_WINNT>=0x0502
// WINVER>=0x0502
// 
// Windows XP _WIN32_WINNT>=0x0501
// WINVER>=0x0501
// 
// Windows 2000 _WIN32_WINNT>=0x0500
// WINVER>=0x0500
// 
// Windows NT 4.0 _WIN32_WINNT>=0x0400
// WINVER>=0x0400
// 
// Windows Me _WIN32_WINDOWS=0x0500
// WINVER>=0x0500
// 
// Windows 98 _WIN32_WINDOWS>=0x0410
// WINVER>=0x0410
// 
// Windows 95 _WIN32_WINDOWS>=0x0400
// WINVER>=0x0400
// 
// Internet Explorer 7.0 _WIN32_IE>=0x0700 
// Internet Explorer 6.0 SP2 _WIN32_IE>=0x0603 
// Internet Explorer 6.0 SP1 _WIN32_IE>=0x0601 
// Internet Explorer 6.0 _WIN32_IE>=0x0600 
// Internet Explorer 5.5 _WIN32_IE>=0x0550 
// Internet Explorer 5.01 _WIN32_IE>=0x0501 
// Internet Explorer 5.0, 5.0a, 5.0b _WIN32_IE>=0x0500 
// Internet Explorer 4.01 _WIN32_IE>=0x0401 
// Internet Explorer 4.0 _WIN32_IE>=0x0400 
// Internet Explorer 3.0, 3.01, 3.02 _WIN32_IE>=0x0300 

//------------------------------------------------------

#ifndef _WIN32_WINNT
#define _WIN32_WINNT	_WIN32_WINNT_WIN2K	// Windows 2000
#endif

#ifndef WINVER
#define WINVER			_WIN32_WINNT
#endif

#ifndef _WIN32_IE
#define _WIN32_IE		_WIN32_IE_WIN2K
#endif

#ifndef NTDDI_VERSION
#define NTDDI_VERSION	NTDDI_WIN2K
#endif

//------------------------------------------------------
// STL:

// _SECURE_SCL
// Defines whether Checked Iterators are enabled. If defined as 1, unsafe iterator use causes
// a runtime error. If defined as 0, checked iterators are disabled. The exact behavior of
// the runtime error depends on the value of _SECURE_SCL_THROWS. The default value for _SECURE_SCL
// is 1, meaning checked iterators are enabled by default.

#ifdef _DEBUG
#define _SECURE_SCL			1	// To enable checked iterators
#endif

// _SECURE_SCL_THROWS
// Defines whether incorrect use of Checked Iterators causes an exception or a program termination.
// If defined as 1, an out of range iterator use causes an exception at runtime. If defined as 0,
// the program is terminated by calling invalid_parameter. The default value for _SECURE_SCL_THROWS is 0,
// meaning the program will be terminated by default. Requires _SECURE_SCL to also be defined.

#if _MSC_VER < 1600
// _SECURE_SCL_THROWS is deprecated since MSVC 2010
#define _SECURE_SCL_THROWS	1
#endif // _MSC_VER > 1000

//------------------------------------------------------

#ifndef WIN32
#define WIN32
#endif

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif

#ifdef _UNICODE
#ifndef UNICODE
#define UNICODE         // UNICODE is used by Windows headers
#endif
#endif

#ifdef UNICODE
#ifndef _UNICODE
#define _UNICODE        // _UNICODE is used by C-runtime/MFC headers
#endif
#endif

#ifdef VC_EXTRALEAN
#define WIN32_EXTRA_LEAN
#define NOSERVICE
#define NOMCX
#define NOIME
#define NOSOUND
#define NOCOMM
#define NOKANJI
#define NORPC
#define NOPROXYSTUB
#define NOIMAGE
#define NOTAPE

#ifndef NO_ANSIUNI_ONLY
#ifdef _UNICODE
#define UNICODE_ONLY
#else
#define ANSI_ONLY
#endif
#endif //!NO_ANSIUNI_ONLY

#endif //VC_EXTRALEAN

//------------------------------------------------------
