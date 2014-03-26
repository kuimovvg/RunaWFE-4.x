// Tracing.cpp: implementation of the Tracing class.
// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "Tracing.h"
#define TRACE_HOOKS
#ifdef TRACE_HOOKS
#include "TracingInterface.h"
#endif //ifdef TRACE_HOOKS

#include "SafeCriticalSection.h"
#include "TextHelpers.h"
#include "FileHelpers.h"
#include "TimeHelpers.h"
//#include "SmartProfiler.h"
//#include "thread\ThreadManager.h"

//#include "MessageBox.h"
//#include "XMessageBox.h"

//#include "encode\crypt_macro2.h"
#define CS(str)	str

// =====================================
// STUBS:
#define DecodeCrypt2String(str) str
#define STR_CRYPT2(str)	str
#define GetCurrentCallStack() ""
#define GetCurrentThreadName() ""
#define USE_MessageBox
#define MB_TIMEOUT		0x8000000	// some big bit

// =====================================

#include <stdio.h>

//////////////////////////////////////////////////////////////////////

void DumpErrorString(LPCSTR pszString)	// placed here to hide crypt_macro2.cpp module information!!!
{
	MYTRACE("ERROR! Invalid string: \"%s\"!\n", pszString);
//	MYTRACE("ERROR! Invalid string: \"%s\"!\n", std::string(pszString, 20).c_str());
	MYASSERT(FALSE && "Invalid string!");
}

void DumpErrorString(LPCWSTR pszString)	// placed here to hide crypt_macro2.cpp module information!!!
{
	MYTRACE("ERROR! Invalid string: \"%ls\"!\n", pszString);
//	MYTRACE("ERROR! Invalid string: \"%ls\"!\n", std::wstring(pszString, 20).c_str());
	MYASSERT(FALSE && "Invalid string!");
}

//////////////////////////////////////////////////////////////////////

//#define WRITE_GLOBAL_TRACE_LOG

#define DEFAULT_ASSERT_TIMEOUT_SEC	1*60*60	// in seconds

//////////////////////////////////////////////////////////////////////

void CTracer::Trace (bool bCrypted, const char *format, ...)
{
	const int nErrno = errno;
	const LONG le = GetLastError();
	{
		std::string sMsg;
		{
			std::string fmt;
			if(bCrypted)
				fmt = DecodeCrypt2String(format);
			else
				fmt = format;
			va_list arglist;
			va_start(arglist, format);
			sMsg = StdString::FormatV(fmt.c_str(), arglist);
			va_end(arglist);
		}

		// Trim possible last '\n' char:
		if(!sMsg.empty())
		{
			if(sMsg[sMsg.length()-1] == '\n')
			{
				sMsg.erase(sMsg.length()-1);
			}
		}

		TraceInternal(sMsg.c_str(), false, DEFAULT_ASSERT_TIMEOUT_SEC, MB_DEFBUTTON1);
	}
	SetLastError(le);
	errno = nErrno;
}

//////////////////////////////////////////////////////////////////////////

bool CTracer::Assert(bool bCrypted, bool bExpression, LPCSTR szExpression, DWORD dwAssertTimeoutSec, UINT nDefaultButton)
{
	const int nErrno = errno;
	const LONG le = GetLastError();
	if(!bExpression)
	{
		std::string sExpression;
		if(bCrypted)
			sExpression = DecodeCrypt2String(szExpression);
		else
			sExpression = szExpression;

		const DWORD dwTimeout = dwAssertTimeoutSec == 0 ? DEFAULT_ASSERT_TIMEOUT_SEC : dwAssertTimeoutSec;
		const UINT nDefaultBtn = nDefaultButton == 0 ? MB_DEFBUTTON1 : nDefaultButton;
		TraceInternal(sExpression.c_str(), true, dwTimeout, nDefaultBtn);
	}
	SetLastError(le);
	errno = nErrno;
	return bExpression;
}

//////////////////////////////////////////////////////////////////////////

void CTracer::MakeDump(bool bCrypted, LPCSTR szExpression)
{
	std::string sExpression;
	if(bCrypted)
		sExpression = DecodeCrypt2String(szExpression);
	else
		sExpression = szExpression;

	const int nErrno = errno;
	const LONG le = GetLastError();
	TraceInternal(sExpression.c_str(), true, DEFAULT_ASSERT_TIMEOUT_SEC, MB_DEFBUTTON1);
	SetLastError(le);
	errno = nErrno;
}

//////////////////////////////////////////////////////////////////////////

bool CTracer::CheckEqual(const std::string& sExpected, const std::string& sActual)
{
	if(sExpected != sActual)
	{
		Trace(true, STR_CRYPT2("Expected \"%s\" but got \"%s\"."), sExpected.c_str(), sActual.c_str());
		return false;
	}
	return true;
}

//////////////////////////////////////////////////////////////////////////

bool CTracer::CheckEqual(int Expected, int Actual)
{
	if(Expected != Actual)
	{
		Trace(true, STR_CRYPT2("Expected %d but got %d."), Expected, Actual);
		return false;
	}
	return true;
}

//////////////////////////////////////////////////////////////////////////

bool CTracer::CheckNotEqual(const std::string& sExpected, const std::string& sActual)
{
	if(sExpected == sActual)
	{
		Trace(true, STR_CRYPT2("Expected result not equal to \"%s\" but got it."), sExpected.c_str());
		return false;
	}
	return true;
}

//////////////////////////////////////////////////////////////////////////

bool CTracer::CheckNotEqual(int Expected, int Actual)
{
	if(Expected == Actual)
	{
		Trace(true, STR_CRYPT2("Expected result not equal to %d but got it."), Expected);
		return false;
	}
	return true;
}

//////////////////////////////////////////////////////////////////////////

GlobalCriticalSection	g_protectConsoleWriting;
//GlobalCriticalSection	g_protectTraceWriting;
GlobalCriticalSection	g_protectMessageBox;

//////////////////////////////////////////////////////////////////////////

#ifdef TRACE_HOOKS

GlobalCriticalSection	g_protectAdditionalTracer;
GlobalCriticalSection	g_protectAdditionalFileTracer;

PFN_WriteTracerAdditional		g_pfnAdditionalTracer = NULL;
LPVOID							g_pAdditionalTracerArg = NULL;

PFN_WriteTracerAdditionalFile	g_pfnAdditionalFileTracer = NULL;
LPVOID							g_pAdditionalFileTracerArg = NULL;

PFN_OnAssertMessageBox			g_pfnOnAssertMessageBox = NULL;
LPVOID							g_pOnAssertMessageBoxArg = NULL;

//////////////////////////////////////////////////////////////////////////

void SetAdditionalTracer(PFN_WriteTracerAdditional pfnAdditionalTracer, LPVOID pAdditionalTracerArg)
{
	SafeCSLock lock(g_protectAdditionalTracer);
	g_pAdditionalTracerArg = pAdditionalTracerArg;
	g_pfnAdditionalTracer = pfnAdditionalTracer;
}

//////////////////////////////////////////////////////////////////////////

void SetAdditionalTracerFile(PFN_WriteTracerAdditionalFile pfnAdditionalTracer, LPVOID pAdditionalTracerArg)
{
	SafeCSLock lock(g_protectAdditionalFileTracer);
	g_pAdditionalFileTracerArg = pAdditionalTracerArg;
	g_pfnAdditionalFileTracer = pfnAdditionalTracer;
}

//////////////////////////////////////////////////////////////////////////

void SetAssertMessageBoxHandler(PFN_OnAssertMessageBox pfnOnAssertMessageBox, LPVOID pArg)
{
	SafeCSLock lock(g_protectMessageBox);
	g_pfnOnAssertMessageBox = pfnOnAssertMessageBox;
	g_pOnAssertMessageBoxArg = pArg;
}

#endif //ifdef TRACE_HOOKS

//////////////////////////////////////////////////////////////////////////

void LocalWriteTracerAdditionalFile(LPCVOID pData, int nBytes) // binary data, text is CRLF-formatted before
{
#ifdef TRACE_HOOKS
	SafeCSLock lock(g_protectAdditionalFileTracer);
	if(NULL != g_pfnAdditionalFileTracer)
	{
		g_pfnAdditionalFileTracer(g_pAdditionalFileTracerArg, pData, nBytes);
	}
#endif //ifdef TRACE_HOOKS
}

//////////////////////////////////////////////////////////////////////////

class MainLogger
{
public:
	MainLogger()
	{
		m_pMainLogFile = NULL;
		m_bFirstWrite = true;
		m_bBusy = false;
	}

	~MainLogger()
	{
		if(NULL != m_pMainLogFile)
		{
			fclose(m_pMainLogFile);
		}
		m_pMainLogFile = NULL;
	}

	void SaveString(const std::string& sText)
	{
#ifdef WRITE_GLOBAL_TRACE_LOG
		SafeCSLock lock(m_protectFileWriting);
		if(m_bFirstWrite)
		{
			m_bFirstWrite = false;
			TCHAR szLog[MAX_PATH] = _T("");
			GetModuleFileName( NULL, szLog, MAX_PATH );
			_tcscat_s(szLog, MAX_PATH, ".log");

			m_pMainLogFile = my_fopen_aplus(szLog, "ab+");
			if(NULL != m_pMainLogFile)
			{
				const std::string sToFile = "\r\n===============================================\r\n";
				fwrite(sToFile.c_str(), sToFile.length(), 1, m_pMainLogFile);
				fflush(m_pMainLogFile);
			}
			else
			{
				if(!m_bBusy) // to exclude recursion
				{
					m_bBusy = true;
					_tprintf(_T("Can't open file '%s'!\n"), szLog);
					fflush(stdout);
					MYASSERT(FALSE && "Can't open main log file! (IGNORE IS IGNORE)");
					m_bBusy = false;
				}
			}
		}
#endif

		const std::string sDosText = lf2crlf(sText);

#ifdef WRITE_GLOBAL_TRACE_LOG
		if(NULL != m_pMainLogFile)
		{
			fwrite(sDosText.c_str(), sDosText.length(), 1, m_pMainLogFile);
			fflush(m_pMainLogFile);
		}
#endif

		LocalWriteTracerAdditionalFile(sDosText.c_str(), sDosText.length());
	}

protected:
	SafeCriticalSection	m_protectFileWriting;
	FILE*	m_pMainLogFile;
	bool	m_bFirstWrite;
	bool	m_bBusy;
};

//MainLogger g_MainLogger;
Singleton<MainLogger>	g_MainLogger;

#ifdef USE_SPECIFIC_PROCESSINFO
std::string CTracer::g_sSpecificProcessInfo = "<nothing_specific>";
#endif

//////////////////////////////////////////////////////////////////////////

void FreeTracingObjects()
{
	CTracer::SetSpecificProcessInfo(""); // clear CTracer::g_sSpecificProcessInfo
	g_MainLogger.clear();
}

//////////////////////////////////////////////////////////////////////////

std::string PadTabs(const std::string& s, size_t nWidthInTabs, size_t nTabWidth = 8)
{
	const size_t len = s.length();
	if(len > nWidthInTabs * nTabWidth)
		return s;
	return s + std::string(nWidthInTabs - len/nTabWidth, '\t');
}

//////////////////////////////////////////////////////////////////////////

#define REASON_CUSTOM	100		// NOTE! should be defined as enum in tracing.h

//////////////////////////////////////////////////////////////////////////

std::string CTracer::FormatReason(const CTracer::TRACE_REASON reason)
{
	switch(reason)
	{
	case REASON_ASSERT:		return CS("ASSERT");
	case REASON_WARNING:	return CS("WARNING");
	case REASON_TRACE:		return CS("Trace");
	case REASON_DUMP:		return CS("DBG_DUMP");
	case REASON_CUSTOM:		return CS("CUSTOM");
	}
	MessageBoxA(NULL, CS("Unknown CTracer reason!"), NULL, MB_OK);
	return StdString::Format(CS("UNKNOWN_%d"), reason);
}

//////////////////////////////////////////////////////////////////////////

std::string CleanFullFileName(const std::string& sFileName)
{
	// Remove first part of sFileName containing 4 '\' chars:

	if(sFileName.length() < 10)
		return sFileName;
	if(sFileName.substr(1,2) != ":\\")
		return sFileName;

	size_t pos = 0;
	for(size_t i = 0; i < 4; i++)
	{
		size_t pos2 = sFileName.find('\\', pos);
		if(pos2 == std::string::npos)
			break;
		pos = pos2 + 1;
	}

	return sFileName.substr(pos);
}

//////////////////////////////////////////////////////////////////////////

void CTracer::TraceInternal (const LPCSTR szMessage, const bool bCompilerLogCompliant,
							 const DWORD dwAssertTimeoutSec, const UINT nDefaultButton)
{
	const std::string& sTime = GetStringDateTime();
	const std::string& sReason = FormatReason(m_reason);

	bool bSendDumpMessage = false;
	bool bShowMessageBox = false;
	switch(m_reason)
	{
	case REASON_ASSERT:
		bSendDumpMessage = true;
		bShowMessageBox = true;
		break;
	case REASON_WARNING:
		break;
	case REASON_TRACE:
		break;
	case REASON_DUMP:
	case REASON_CUSTOM:
		bSendDumpMessage = true;
		break;
	default:
		bSendDumpMessage = true;
		bShowMessageBox = true;
		break;
	}

#ifdef CRYPT_TRACE_SOURCES_INFO
	const std::string& sFile = CleanFullFileName(DecodeCrypt2String(m_szFile));
#else
	const std::string& sFile = CleanFullFileName(m_szFile);
#endif
	const std::string& sFileNameOnly = StringToUtf8(GetFileName(Utf8ToString(sFile)));
	const std::string& sCallStack = bShowMessageBox ? GetCurrentCallStack() : "";

	const std::string& sThreadName = GetCurrentThreadName();

	const std::string& sFileLine = StdString::Format("%s(%d)", sFileNameOnly.c_str(), m_nLine);
	const std::string& sFileNamePlusLineWhole = PadTabs(sFileLine,4);

#ifdef CRYPT_TRACE_SOURCES_INFO
	const std::string sFunction = m_szFunction == NULL ? "" : DecodeCrypt2String(m_szFunction);
#else
	const std::string sFunction = m_szFunction == NULL ? "" : m_szFunction;
#endif
	const std::string& sWhole =
		StdString::Format(CS("%s\t%s%s %s %s %s"),
		sTime.c_str(), sFileNamePlusLineWhole.c_str(),
		(sFunction.empty() ? "" : PadTabs(" " + sFunction,6).c_str()),
		sThreadName.c_str(), sReason.c_str(), szMessage);

	const std::string& sShort =
		StdString::Format(CS("%s(%d) %s %s"),
		(bCompilerLogCompliant ? sFile.c_str() : sFileNameOnly.c_str()),
		m_nLine, sReason.c_str(), szMessage);

	bool bWriteToConsole = true;
	{
#ifdef TRACE_HOOKS
		SafeCSLock lock(g_protectAdditionalTracer);
		if(NULL != g_pfnAdditionalTracer)
		{
			bWriteToConsole = g_pfnAdditionalTracer(g_pAdditionalTracerArg, sShort.c_str(), sShort.length());
		}
#endif //ifdef TRACE_HOOKS
	}

	if(bWriteToConsole)
	{
		SafeCSLock lock(g_protectConsoleWriting);
		if(!sCallStack.empty())
		{
			printf(CS("Callstack: %s\n"), sCallStack.c_str());
		}
		//printf("%s\n", sShort.c_str());
		puts(sShort.c_str());
		fflush(stdout);
	}

// 	{
// 		SafeCSLock lock(g_protectTraceWriting);
// 		OutputDebugString((sWhole + "\n").c_str());
// 		//	TRACE("%s\n", sWhole.c_str());
// 	}

	if(!sCallStack.empty())
	{
		g_MainLogger.get().SaveString(StdString::Format(CS("%s\tCallstack:\t%s\n"), sTime.c_str(), sCallStack.c_str()));
	}
	const std::string& sToFile = StdString::Format("%s\n", sWhole.c_str());
	g_MainLogger.get().SaveString(sToFile);

//#ifdef _DEBUG
	{
// 		{
// 			SafeCSLock lock(g_protectConsoleWriting);
// 			printf("entering protectMessageBox in %s...\n", sThreadName.c_str());
// 		}
		// Block any thread after tracing until ASSERT window is closed:
		SafeCSLock lock(g_protectMessageBox);
// 		{
// 			SafeCSLock lock(g_protectConsoleWriting);
// 			printf("entered protectMessageBox in %s!\n", sThreadName.c_str());
// 		}

		if(bSendDumpMessage || bShowMessageBox)
		{
			MYTRACE("Begin of messagebox critical section...\n");
			if(bSendDumpMessage)
			{
				MYTRACE("bSendDumpMessage...\n");
			}
			if(bShowMessageBox)
			{
				MYTRACE("bShowMessageBox... (%d min, default button: #%d)\n",
					dwAssertTimeoutSec/60,
					(nDefaultButton == MB_DEFBUTTON1 ? 1: 
					nDefaultButton == MB_DEFBUTTON2 ? 2: 
					nDefaultButton == MB_DEFBUTTON3 ? 3: nDefaultButton)
					);
			}
			const bool bShortAssert = dwAssertTimeoutSec < DEFAULT_ASSERT_TIMEOUT_SEC;
			bool bExecuteDefaultHandler = true;
#ifdef TRACE_HOOKS
			if(NULL != g_pfnOnAssertMessageBox)
			{
				LPCSTR szMsgBoxText = REASON_CUSTOM == m_reason ? szMessage : sWhole.c_str();
				bExecuteDefaultHandler = g_pfnOnAssertMessageBox(szMsgBoxText,
					g_pOnAssertMessageBoxArg, true, bShortAssert, m_reason);
			}
#endif //ifdef TRACE_HOOKS

			MYTRACE("bExecuteDefaultHandler = %d\n", bExecuteDefaultHandler);
			if(bExecuteDefaultHandler && bShowMessageBox)
			{
				const std::string sHelp =
					StdString::Format(
//					"\nPress any button below:"
//					"\nAbort - to stop application"
//					"\nRetry - to start debugging"
//					"\nIgnore - to continue working"
					"\nPID %d: %s"
					"\n%s"
					,
					GetCurrentProcessId(), GetCommandLine(), GetSpecificProcessInfo().c_str()
					);

				int nRes = IDABORT;
				MYTRACE("MessageBox(%d min)...\n", dwAssertTimeoutSec/60);
#ifdef USE_MessageBox
				nRes = MessageBoxA(NULL, (sWhole + sHelp).c_str(), sReason.c_str(),
					MB_ABORTRETRYIGNORE | MB_SETFOREGROUND | MB_ICONWARNING);
#endif //ifdef USE_MessageBox
#ifdef USE_WinMessageBox
				nRes = WinMessageBox(NULL, (sWhole + sHelp).c_str(), sReason.c_str(),
//					MB_ABORTRETRYIGNORE | 
					MB_TERMINATEDEBUGIGNORE |
					MB_SETFOREGROUND | MB_ICONWARNING,
					dwAssertTimeoutSec*1000, IDABORT);
#endif //ifdef USE_WinMessageBox
#ifdef USE_XMessageBox

				XMSGBOXPARAMS xmb;
				xmb.nTimeoutSeconds = dwAssertTimeoutSec; 
				xmb.bStopTimerOnClick = FALSE;
				xmb.bUseUserDefinedButtonCaptions = TRUE;
#define COPY_BUTTON_NAME(var, name) _tcscpy_s(var, sizeof(var)/sizeof(var[0]), name)
				COPY_BUTTON_NAME(xmb.UserDefinedButtonCaptions.szAbort, _T("&Terminate"));
				COPY_BUTTON_NAME(xmb.UserDefinedButtonCaptions.szRetry, _T("&Debug"));
				COPY_BUTTON_NAME(xmb.UserDefinedButtonCaptions.szIgnore, _T("&Continue"));
				nRes = XMessageBox(NULL,
					(sWhole + sHelp).c_str(),
					sReason.c_str(),
					MB_ABORTRETRYIGNORE | nDefaultButton |
					MB_SETFOREGROUND | MB_ICONWARNING | MB_NORESOURCE, &xmb);
#endif // ifdef USE_XMessageBox
				MYTRACE("MessageBox returned %d (0x%X)\n", nRes, nRes);

				const bool bTimeout = 0 != (nRes & MB_TIMEOUT);
				nRes &= ~MB_TIMEOUT; // clear MB_TIMEOUT bit

				switch(nRes)
				{
				case IDIGNORE:		// MB_ABORTRETRYIGNORE
				case IDCONTINUE:	// MB_CANCELTRYCONTINUE
					break;

				case IDRETRY:		// MB_ABORTRETRYIGNORE
				case IDTRYAGAIN:	// MB_CANCELTRYCONTINUE
					MYTRACE("DEBUG! INT 3!\n");
					__asm{int 3};
					break;

				case IDCANCEL:		// MB_CANCELTRYCONTINUE
				case IDABORT:		// MB_ABORTRETRYIGNORE
//				case IDCANCEL | MB_TIMEOUT:	// MB_CANCELTRYCONTINUE
//				case IDABORT | MB_TIMEOUT:	// MB_ABORTRETRYIGNORE
					MYTRACE("Aborting...\n");
					exit(2);
					break;
				default:
					MYTRACE("ERROR: Invalid Messagebox result! (%d)\n", nRes);
					MYASSERT(FALSE && "Invalid Messagebox result!");
				}
			}
			else
			{
				MYTRACE("Skipping MessageBox()...\n");
			}

#ifdef TRACE_HOOKS
			if(NULL != g_pfnOnAssertMessageBox)
			{
				g_pfnOnAssertMessageBox(sWhole.c_str(),
					g_pOnAssertMessageBoxArg, false, bShortAssert, m_reason);
			}
#endif //ifdef TRACE_HOOKS

			MYTRACE("End of messagebox critical section!\n");
		}
	}
//#endif
}

//////////////////////////////////////////////////////////////////////////
