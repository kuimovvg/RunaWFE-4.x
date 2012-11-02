/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.bot;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;

@Entity
@Table(name = "BOT")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Bot implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private BotStation botStation;
    private String wfeUser;
    private String wfePass;
    private Long maxPeriod;
    private Long lastInvoked;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BOT")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(targetEntity = BotStation.class)
    @JoinColumn(name = "BOT_STATION_ID", nullable = false, updatable = true, insertable = true)
    @Index(name = "B_BS_IDX")
    @Fetch(FetchMode.JOIN)
    public BotStation getBotStation() {
        return botStation;
    }

    public void setBotStation(BotStation bs) {
        botStation = bs;
    }

    /**
     * Username for authentification on WFE server.
     */
    @Column(name = "WFE_USER")
    public String getWfeUser() {
        return wfeUser;
    }

    public void setWfeUser(String wfeUser) {
        this.wfeUser = wfeUser;
    }

    @Column(name = "WFE_PASS")
    public String getWfePass() {
        return wfePass;
    }

    public void setWfePass(String wfePass) {
        this.wfePass = wfePass;
    }

    /**
     * Used for periodic bot invocation. If set to 0, periodic invocation on this bot is disabled.
     */
    @Column(name = "MAX_PERIOD")
    public Long getMaxPeriod() {
        return maxPeriod;
    }

    public void setMaxPeriod(Long maxPeriod) {
        this.maxPeriod = maxPeriod;
    }

    /**
     * Last time when this bot was invoked successfully. Used for periodic bot invocation.
     */
    @Column(name = "LAST_INVOKED")
    public Long getLastInvoked() {
        return lastInvoked;
    }

    public void setLastInvoked(Long lastInvoked) {
        this.lastInvoked = lastInvoked;
    }
}
