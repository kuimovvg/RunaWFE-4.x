<?xml version="1.0" encoding="utf-8"?>
<x:stylesheet
 	version="1.0"
	xmlns:x="http://www.w3.org/1999/XSL/Transform"
>
	<x:output method="xml" indent="yes"/>
	
	<x:param name="build.number"/>
	<x:param name="build.dir"/>
	<x:param name="start.time"/>
	
	<x:template match="text()|@*|*|processing-instruction()|comment()" priority="-1">
        <x:copy>
			<x:apply-templates select="text()|@*|*|processing-instruction()|comment()"/>
        </x:copy>
    </x:template>
    <x:template match="tbody">
    	<x:copy>
    		<x:element name="tr">
    			<x:element name="td">
    				<x:attribute name="id">bn-<x:value-of select="$build.number"/></x:attribute>
    				<x:value-of select="$build.number"/>
    			</x:element>
    			<x:element name="td">
    				<x:attribute name="id">st-<x:value-of select="$build.number"/></x:attribute>
   					<x:value-of select="$start.time"/>
    			</x:element>
    			<x:element name="td">
    				<x:attribute name="id">ft-<x:value-of select="$build.number"/></x:attribute>
    			</x:element>
    			<x:element name="td">
    				<x:attribute name="id">ss-<x:value-of select="$build.number"/></x:attribute>
    				STARTING
    			</x:element>
    			<x:element name="td">
    				<x:attribute name="id">sr-<x:value-of select="$build.number"/></x:attribute>
    			</x:element>
    			<x:element name="td">
    				<x:attribute name="id">tr-<x:value-of select="$build.number"/></x:attribute>
    			</x:element>
    			<x:element name="td">
	    			<x:attribute name="id">bl-<x:value-of select="$build.number"/></x:attribute>
    				<x:element name="a">
    					<x:attribute name="href">./<x:value-of select="$build.dir"/>/build.log</x:attribute>
    					log
    				</x:element>
    			</x:element>
    		</x:element>
    		<x:apply-templates/>
    	</x:copy>
    </x:template>
</x:stylesheet>