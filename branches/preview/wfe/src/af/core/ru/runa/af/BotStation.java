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
package ru.runa.af;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "BOT_STATIONS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BotStation extends IdentifiableBaseImpl implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final BotStation SECURED_INSTANCE = new BotStation(0L);

    private Long id;
    private String name;
    private String address;
    private String bsUser;
    private String bsPass;
    private Long version;

    public BotStation() {}

    public BotStation(String name) {
        this.name = name;
    }

    public BotStation(String name, String address) {
        this(name);
        this.address = address;
    }

    public BotStation(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BOT_STATIONS")
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NAME", unique = true, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "ADDRESS")
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Column(name = "BS_USER")
    public String getBsUser() {
        return bsUser;
    }

    public void setBsUser(String bsUser) {
        this.bsUser = bsUser;
    }

    @Column(name = "BS_PASS")
    public String getBsPass() {
        return bsPass;
    }

    public void setBsPass(String bsPass) {
        this.bsPass = bsPass;
    }

    @Column(name = "VERSION")
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
