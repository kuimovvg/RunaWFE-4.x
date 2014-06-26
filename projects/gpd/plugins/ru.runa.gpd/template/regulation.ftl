<html>
	<head>
		<title> ${proc.getName()} </title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	</head>
	<body>
		<h1> Регламент выполнения бизнес-процесса </h1>
		<h1> ${proc.getName()} </h1>
		
		
		<#if proc.getDescription() ?? >
			<p>  Краткое описание бизнес-процесса: ${proc.getDescription()} </p>
		</#if>
		
		<#if brief ?? >
			<p> Подробное описание бизнес-процесса: </p>
			${brief}
		</#if>
		
		<#-- SWIMLANES -->
		<p> Список ролей бизнес-процесса: </p>
		<ul>
		<#list proc.getSwimlanes() as swimlane >
			<li> ${swimlane.getName()} </li>
		</#list>
		</ul>
		<br>
		
		<#-- VARIABLES -->
		<p> Cписок переменных бизнес-процесса: </p>
		<ul>
			<#list proc.getVariables(true,false,null) as var>
				<li> ${var.getName()} </li>
			</#list>
		</ul>
		<br>
		
		<h2> Описание действий бизнес-процесса: </h2>
		<br>
		
		<#-- START POINT -->
		<#assign start = proc.getChildrenRecursive(model.start)?first >
		<p> Начало выполнения бизнес-процесса: ${start.getName()} </p>
		<#if start.getSwimlane() ?? >
			<p> Роль: ${start.getSwimlane().getName()}</p>
		</#if>
		<#assign afterStart = start.getLeavingTransitions()?first >
		<p> Далее управление переходит к шагу <a href="#${afterStart.getTarget().getId()}">${afterStart.getTarget().getName()}</a></p>
		<br>
		
		<#-- NODES -->
		<#list proc.getChildren(model.node) as node>
			
			
			<#-- TaskState -->
			<#if node.class.simpleName == "TaskState" || node.class.simpleName == "Decision"  || node.class.simpleName == "Conjunction">
				
				<#-- name -->
				<p id="${node.getId()}" >Шаг: ${node.getName()}</p>
				
				<#-- type -->
				<#if node.class.simpleName == "Conjunction">
					<p>Тип шага: Соединение</p>
				<#elseif node.getLeavingTransitions()?size == 1>
					<p>Тип шага: Действие</p>
				<#else>
					<p>Тип шага: Ветвление</p>
				</#if>
				
				<#-- swimlane -->
				<#if node.class.simpleName == "TaskState" && node.getSwimlane() ?? >
					<p> Роль: ${node.getSwimlane().getName()}</p>
				</#if>
				
				<#-- transitions -->
				<#if node.getLeavingTransitions()?size == 1>
					<#assign afterTask = node.getLeavingTransitions()?first >
					<p> Далее управление переходит к шагу <a href="#${afterTask.getTarget().getId()}">${afterTask.getTarget().getName()}</a></p>
				<#else>
					<p>Далее управление переходит:</p>    
					<ul>
						<#list node.getLeavingTransitions() as transition>
							<p>в случае ${transition.getName()} <a href="#${transition.getTarget().getId()}">${transition.getTarget().getName()}</a> </p>
						</#list> 
					</ul>
				</#if>
				
				<#-- timer option -->
				<#if node.getTimer() ?? >
					<#assign timer = node.getTimer() >
					<#assign afterTimer = timer.getLeavingTransitions()?first>
					<p> 
						В случае задержки задания на ${timer.getPropertyValue("timerDelay").toString()} времени управление переходит к шагу 
						<a href="#${afterTimer.getTarget().getId()}">${afterTimer.getTarget().getName()}</a>
					</p>
				</#if>
			</#if>
			
			<#-- ParallelGateway -->
			<#if node.class.simpleName == "ParallelGateway" || node.class.simpleName == "Fork">
			
				<#-- name -->
				<p id="${node.getId()}" > Шаг: Разделение ${node.getName()}</p>
				
				<#-- type -->
				<p>Тип шага: Разделение</p>
				
				<#-- transitions -->
				<p>Далее создается ${node.getLeavingTransitions()?size} точек управления, которые переходят в</p>    
				<#list node.getLeavingTransitions() as transition>
					<p> <a href="#${transition.getTarget().getId()}">${transition.getTarget().getName()}</a> </p>
				</#list> 
			</#if>
			
			<#-- ExclusiveGateway -->
			<#if node.class.simpleName == "ExclusiveGateway" || node.class.simpleName == "Join">
			
				<#-- name -->
				<p id="${node.getId()}" > Шаг: Соединение ${node.getName()}</p>
				
				<#-- type -->
				<p>Тип шага: Соединение</p>
				
				<#-- transitions -->
				<#assign afterNode = node.getLeavingTransitions()?first >
				<p>
					Далее cоединяются ${node.getArrivingTransitions()?size} точек управления, и управление переходит к шагу
					<a href="#${afterNode.getTarget().getId()}">${afterNode.getTarget().getName()}</a> 
				</p>    
			</#if>
			
			<#-- Timer -->
			<#if node.class.simpleName == "Timer" >
			
				<#-- name -->
				<p id="${node.getId()}" > Шаг: ${node.getName()}</p>
				
				<#-- type -->
				<p>Тип шага: Таймер</p>
				
				<#-- transitions -->
				<#assign afterNode = node.getLeavingTransitions()?first >
				<p> 
					После истечении ${node.getPropertyValue("timerDelay").toString()} времени управление переходит к шагу 
					<a href="#${afterNode.getTarget().getId()}">${afterNode.getTarget().getName()}</a>
				</p>
			</#if>
			
			<#-- Receive & Send message -->
			<#if node.class.simpleName == "ReceiveMessageNode" || node.class.simpleName == "SendMessageNode">
			
				<#-- name -->
				<p id="${node.getId()}" > Шаг: ${node.getName()}</p>
				
				<#-- type -->
				<#if node.class.simpleName == "ReceiveMessageNode">
					<p>Тип шага: Прием сообщения</p>
				<#else>
					<p> Тип шага: Отправка сообщения </p>
				</#if>
				
				<#-- transitions -->
				<#assign afterNode = node.getLeavingTransitions()?first >
				<p>
					После отправки сообщения управление переходит к шагу 
					<a href="#${afterNode.getTarget().getId()}">${afterNode.getTarget().getName()}</a>
				</p>
				
				<#-- timer option -->
				<#if node.class.simpleName == "ReceiveMessageNode"  && node.getTimer()?? >
					<#assign timer = node.getTimer() >
					<#assign afterTimer = timer.getLeavingTransitions()?first>
					<p> 
						В случае задержки задания на ${timer.getPropertyValue("timerDelay").toString()} времени управление переходит к шагу 
						<a href="#${afterTimer.getTarget().getId()}">${afterTimer.getTarget().getName()}</a>
					</p>
				</#if>
				
				<#-- msg live time option -->
				<#if node.class.simpleName == "SendMessageNode" && node.getTtlDuration() ??>
					<p> Время жизни сообщения ${node.getTtlDuration().toString()}</p>
				</#if>
			</#if>
			
			<#-- Subprocess & Multisubprocess -->
			<#if node.class.simpleName == "Subprocess" || node.class.simpleName == "MultiSubprocess">
				
				<#-- name -->
				<p id="${node.getId()}" > Шаг: ${node.getName()}</p>
				
				<#-- type -->
				<#if node.class.simpleName == "Subprocess">
					<p> Тип шага: Запуск подпроцесса</p>
				<#else>
					<p> Тип шага: Запуск мультиподпроцесса </p>
				</#if>
				
				<#-- suprocess name -->
				<#if node.getSubProcessName() != "">
					<p> Имья подпроцесса ${node.getSubProcessName()} </p>
				</#if>
			</#if>
			
			<#--  ScriptTask -->
			<#if node.class.simpleName == "ScriptTask">
			
				<#-- name -->
				<p id="${node.getId()}" > Шаг: ${node.getName()}</p>
				
				<#-- type -->
				<p>Тип шага: Выполнение сценария</p>
			</#if>
					
			
				
			<br>
		
		</#list>
		
		<#-- EndTokenState -->
		<#list proc.getChildrenRecursive(model.endToken) as end>
			<p id="${end.getId()}"> Завершение потока  выполнения бизнес-процесса: ${end.getName()} </p>
			<br>
		</#list>
		
		<#-- EndState -->
		<#list proc.getChildrenRecursive(model.end) as end>
			<p id="${end.getId()}"> Завершение процесса выполнения бизнес-процесса: ${end.getName()} </p>
			<br>
		</#list>
				
		
		
			
	</body>
</html>