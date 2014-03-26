#pragma once
#include "ws/ServerAPIBindingProxy.h"
#include "RtnResources.h"
using namespace std;
namespace Server {

enum ConnectionState {
	CONNECTION_STATE_INITIALIZING,
	CONNECTION_STATE_CONNECTED,
	CONNECTION_STATE_ERROR
};

class State {
private:
	ConnectionState connectionState;
	int totalTasksCount;
	int unreadTasksCount;
	LONG64* taskIds;
	wstring* taskNames;
public:
	State(ConnectionState connectionState);
	State(ns1__getTasksResponse* response);
	~State();
	ConnectionState GetConnectionState();
	int GetTotalTasksCount();
	int GetUnreadTasksCount();
	bool HasChanges(State* previousState);
	wstring GetNotificationMessageAboutNewTasks(State* previousState);
};

class Connector {
public:
	Connector();
	~Connector();
	void Initialize();
	void Reset();
	void UpdateState();
	bool HasChanges();
	bool HasNotificationAboutNewTasks();
	wstring GetNotificationTooltipMessage();
	HICON GetNotificationIcon();
	wstring GetNotificationMessageAboutNewTasks();
private:
	bool initialized;
	wstring serverType;
	wstring serverVersion;
	State* previousState;
	State* currentState;
	ns1__user* user;
	ns1__user* AuthenticateByKerberos();
	State* GetTasks();
	State* Connector::RequestState();
};

}