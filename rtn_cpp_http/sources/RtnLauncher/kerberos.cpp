// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//------------------------------------------------------
#include "stdafx.h"
#include "kerberos.h"

#include "..\common\Tracing.h"
#include "..\common\TextHelpers.h"
#include "..\common\TimeHelpers.h"

#define SECURITY_WIN32
#define WIN32_CHICAGO
#include <Security.h>
#pragma comment (lib, "Secur32.lib")

#ifndef _GSSAPI_H_
typedef unsigned long gss_uint32;
typedef gss_uint32      OM_uint32;
#endif

//------------------------------------------------------

void display_status(char *msg, ULONG maj_stat, ULONG min_stat)
{
	MYTRACE("ERROR: %s failed: 0x%08X (%d): %s\n",
		msg, maj_stat, maj_stat, StdString::FormatApiErrorA(maj_stat).c_str());
}

//------------------------------------------------------
// The following code is based on MS PSDK 2003 sample:
// PSDK\Samples\security\SSPI\GSS\GssClient.c
//------------------------------------------------------

/*
 * Copyright 1994 by OpenVision Technologies, Inc.
 *
 * Permission to use, copy, modify, distribute, and sell this software
 * and its documentation for any purpose is hereby granted without fee,
 * provided that the above copyright notice appears in all copies and
 * that both that copyright notice and this permission notice appear in
 * supporting documentation, and that the name of OpenVision not be used
 * in advertising or publicity pertaining to distribution of the software
 * without specific, written prior permission. OpenVision makes no
 * representations about the suitability of this software for any
 * purpose.  It is provided "as is" without express or implied warranty.
 *
 * OPENVISION DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE,
 * INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO
 * EVENT SHALL OPENVISION BE LIABLE FOR ANY SPECIAL, INDIRECT OR
 * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF
 * USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
 * OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THIS SOFTWARE.
 */

//------------------------------------------------------

/*
 * Function: client_establish_context
 *
 * Purpose: establishes a GSS-API context with a specified service and
 * returns the context handle
 *
 * Arguments:
 *
 *      s               (r) an established TCP connection to the service
 *      service_name    (r) the ASCII service name of the service
 *      context         (w) the established GSS-API context
 *      ret_flags       (w) the returned flags from init_sec_context
 *
 * Returns: 0 on success, -1 on failure
 *
 * Effects:
 *
 * service_name is imported as a GSS-API name and a GSS-API context is
 * established with the corresponding service; the service should be
 * listening on the TCP connection s.  The default GSS-API mechanism
 * is used, and mutual authentication and replay detection are
 * requested.
 *
 * If successful, the context handle is returned in context.  If
 * unsuccessful, the GSS-API error messages are displayed on stderr
 * and -1 is returned.
 */
int client_establish_context(wchar_t *service_name, OM_uint32 deleg_flag,
							 CtxtHandle *gss_context, OM_uint32 *ret_flags,
							 OUT std::string& Token)
{
	//int s, 
	SecBuffer send_tok, recv_tok;
	SecBufferDesc input_desc, output_desc;
	OM_uint32 maj_stat;
	CredHandle cred_handle;
	TimeStamp expiry;
	PCtxtHandle context_handle = NULL;

	input_desc.cBuffers = 1;
	input_desc.pBuffers = &recv_tok;
	input_desc.ulVersion = SECBUFFER_VERSION;

	recv_tok.BufferType = SECBUFFER_TOKEN;
	recv_tok.cbBuffer = 0;
	recv_tok.pvBuffer = NULL;

	output_desc.cBuffers = 1;
	output_desc.pBuffers = &send_tok;
	output_desc.ulVersion = SECBUFFER_VERSION;

	send_tok.BufferType = SECBUFFER_TOKEN;
	send_tok.cbBuffer = 0;
	send_tok.pvBuffer = NULL;

	cred_handle.dwLower = 0;
	cred_handle.dwUpper = 0;

	maj_stat = AcquireCredentialsHandle(
									NULL,						// no principal name
									L"Kerberos",				// package name
									SECPKG_CRED_OUTBOUND,
									NULL,						// no logon id
									NULL,						// no auth data
									NULL,						// no get key fn
									NULL,						// noget key arg
									&cred_handle,
									&expiry
									);
	if (maj_stat != SEC_E_OK)
	{
		display_status("acquiring credentials", maj_stat, 0);
		return -1;
	}

	/*
	* Perform the context-establishement loop.
	*/

	gss_context->dwLower = 0;
	gss_context->dwUpper = 0;

	do
	{
		maj_stat =
		InitializeSecurityContext(
							&cred_handle,
							context_handle,
							service_name,
							deleg_flag,
							0,			// reserved
							SECURITY_NATIVE_DREP,
							&input_desc,
							0,			// reserved
							gss_context,
							&output_desc,
							ret_flags,
							&expiry
							);

		if (recv_tok.pvBuffer)
		{
			free(recv_tok.pvBuffer);
			recv_tok.pvBuffer = NULL;
			recv_tok.cbBuffer = 0;
		}

		context_handle = gss_context;

		if (maj_stat!=SEC_E_OK && maj_stat!=SEC_I_CONTINUE_NEEDED)
		{
			display_status("initializing context", maj_stat, 0);
			FreeCredentialsHandle(&cred_handle);
			return -1;
		}

		if (send_tok.cbBuffer > 0)
		{
			MYTRACE("init_sec_context returned token (size=%d)...\n", send_tok.cbBuffer);
			Token = std::string((const char*)send_tok.pvBuffer, send_tok.cbBuffer);

			//if (send_token(s, &send_tok) < 0)
			//{
			//	FreeContextBuffer(send_tok.pvBuffer);
			//	FreeCredentialsHandle(&cred_handle);
			//	return -1;
			//}
		}

		FreeContextBuffer(send_tok.pvBuffer);
		send_tok.pvBuffer = NULL;
		send_tok.cbBuffer = 0;

		if (maj_stat == SEC_I_CONTINUE_NEEDED)
		{
			MYTRACE("continue needed...\n");
			//if (recv_token(s, &recv_tok) < 0)
			//{
			//	FreeCredentialsHandle(&cred_handle);
			//	return -1;
			//}
			break; // Sergey: initially there was no break here. I don't understand initial logic.
		}

	} while (maj_stat == SEC_I_CONTINUE_NEEDED);

	FreeCredentialsHandle(&cred_handle);
	return 0;
}

//------------------------------------------------------

std::string GetKerberosTicket(const std::wstring& sKerberosTargetName)
{
	std::string Token;
	SECURITY_STATUS maj_stat = 0;
	unsigned long sspi_ret_flags = 0;

	CtxtHandle context;
	ZeroMemory(&context, sizeof(context));

	WCHAR lpTargetName[1024] = L"";
	wcscpy_s(lpTargetName, sKerberosTargetName.c_str());

	maj_stat = client_establish_context(
		lpTargetName,
		//ISC_REQ_MUTUAL_AUTH |
		ISC_REQ_ALLOCATE_MEMORY |
		ISC_REQ_CONFIDENTIALITY |
		//ISC_REQ_REPLAY_DETECT |
		//ISC_REQ_IDENTIFY |
		//ISC_REQ_DATAGRAM |
		0
		,
		&context,
		&sspi_ret_flags,
		Token
		);
	if (!SEC_SUCCESS(maj_stat))  
	{
		MYTRACE("Establishing security context failed!\n");
		return "";
	}

	MYTRACE("Kerberos ticket: %d bytes length!\n", Token.size());
	//dumpBinaryBuffer(Token.data(), Token.size());

	DeleteSecurityContext (&context);
	return Token;
}

//------------------------------------------------------
