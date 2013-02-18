
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for nodeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="nodeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="StartState"/>
 *     &lt;enumeration value="ActionNode"/>
 *     &lt;enumeration value="End"/>
 *     &lt;enumeration value="WaitState"/>
 *     &lt;enumeration value="TaskNode"/>
 *     &lt;enumeration value="Fork"/>
 *     &lt;enumeration value="Join"/>
 *     &lt;enumeration value="Decision"/>
 *     &lt;enumeration value="Subprocess"/>
 *     &lt;enumeration value="MultiSubprocess"/>
 *     &lt;enumeration value="SendMessage"/>
 *     &lt;enumeration value="ReceiveMessage"/>
 *     &lt;enumeration value="EndToken"/>
 *     &lt;enumeration value="MultiTaskNode"/>
 *     &lt;enumeration value="Merge"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "nodeType")
@XmlEnum
public enum NodeType {

    @XmlEnumValue("StartState")
    START_STATE("StartState"),
    @XmlEnumValue("ActionNode")
    ACTION_NODE("ActionNode"),
    @XmlEnumValue("End")
    END("End"),
    @XmlEnumValue("WaitState")
    WAIT_STATE("WaitState"),
    @XmlEnumValue("TaskNode")
    TASK_NODE("TaskNode"),
    @XmlEnumValue("Fork")
    FORK("Fork"),
    @XmlEnumValue("Join")
    JOIN("Join"),
    @XmlEnumValue("Decision")
    DECISION("Decision"),
    @XmlEnumValue("Subprocess")
    SUBPROCESS("Subprocess"),
    @XmlEnumValue("MultiSubprocess")
    MULTI_SUBPROCESS("MultiSubprocess"),
    @XmlEnumValue("SendMessage")
    SEND_MESSAGE("SendMessage"),
    @XmlEnumValue("ReceiveMessage")
    RECEIVE_MESSAGE("ReceiveMessage"),
    @XmlEnumValue("EndToken")
    END_TOKEN("EndToken"),
    @XmlEnumValue("MultiTaskNode")
    MULTI_TASK_NODE("MultiTaskNode"),
    @XmlEnumValue("Merge")
    MERGE("Merge");
    private final String value;

    NodeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static NodeType fromValue(String v) {
        for (NodeType c: NodeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
