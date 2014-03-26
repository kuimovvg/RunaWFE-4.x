// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//------------------------------------------------------
#pragma once

#include "Tracing.h"

//------------------------------------------------------

typedef bool (*PFN_WriteTracerAdditional)(LPVOID pAdditionalTracerArg,
	LPCVOID pData, const int nBytes);  // binary data (single-byte), no special end-line formatting, returns bWriteToConsole

void SetAdditionalTracer(PFN_WriteTracerAdditional pfnAdditionalTracer, LPVOID pAdditionalTracerArg);

//------------------------------------------------------

typedef void (*PFN_WriteTracerAdditionalFile)(LPVOID pAdditionalTracerArg,
			LPCVOID pData, const int nBytes);  // binary data (single-byte), text is CRLF-formatted before

void SetAdditionalTracerFile(PFN_WriteTracerAdditionalFile pfnAdditionalTracer, LPVOID pAdditionalTracerArg);

//------------------------------------------------------

typedef bool (*PFN_OnAssertMessageBox)(LPCSTR szMessage, LPVOID pArg,
			const bool bBeforeMessageBox, const bool bShortAssert,
			const CTracer::TRACE_REASON reason); // returns true to execute default handler next

void SetAssertMessageBoxHandler(PFN_OnAssertMessageBox pfnOnAssertMessageBox, LPVOID pArg);

//------------------------------------------------------

void FreeTracingObjects();

//------------------------------------------------------

