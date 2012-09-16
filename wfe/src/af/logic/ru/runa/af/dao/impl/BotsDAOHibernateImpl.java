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
package ru.runa.af.dao.impl;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ru.runa.InternalApplicationException;
import ru.runa.af.Bot;
import ru.runa.af.BotStation;
import ru.runa.af.BotTask;
import ru.runa.af.dao.BotsDAO;

import com.google.common.base.Strings;

@SuppressWarnings("unchecked")
public class BotsDAOHibernateImpl extends HibernateDaoSupport implements BotsDAO {

    @Override
    public BotStation getBotStation(final BotStation botStation) {
        return getHibernateTemplate().execute(new HibernateCallback<BotStation>() {

            @Override
            public BotStation doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(BotStation.class);
                if (botStation.getId() <= 0 && Strings.isNullOrEmpty(botStation.getName())) {
                    throw new InternalApplicationException("Not enough parameters for BotStation search.");
                }
                if (botStation.getId() > 0) {
                    criteria.add(Restrictions.eq("id", botStation.getId()));
                }
                if (!Strings.isNullOrEmpty(botStation.getName())) {
                    criteria.add(Restrictions.eq("name", botStation.getName()));
                }
                return (BotStation) criteria.uniqueResult();
            }
        });
    }

    @Override
    public Bot getBot(final Bot bot) {
        return getHibernateTemplate().execute(new HibernateCallback<Bot>() {

            @Override
            public Bot doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(Bot.class);
                if (bot.getId() <= 0 && Strings.isNullOrEmpty(bot.getWfeUser()) && bot.getBotStation() == null) {
                    throw new InternalApplicationException("Not enough parameters for Bot search.");
                }

                if (bot.getId() > 0) {
                    criteria.add(Restrictions.eq("id", bot.getId()));
                } else if (bot.getWfeUser() != null) {
                    criteria.add(Restrictions.eq("wfeUser", bot.getWfeUser()));
                } else if (bot.getBotStation() != null) {
                    criteria.add(Restrictions.eq("botStation", bot.getBotStation()));
                } else {
                    throw new InternalApplicationException("Bot should specify id or wfeUser or botStation");
                }
                return (Bot) criteria.uniqueResult();
            }
        });
    }

    @Override
    public BotTask getBotTask(final BotTask botTask) {
        return getHibernateTemplate().execute(new HibernateCallback<BotTask>() {

            @Override
            public BotTask doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(BotTask.class);
                if (botTask.getId() <= 0 && botTask.getBot() == null && botTask.getName() == null) {
                    throw new InternalApplicationException("Not enough parameters for Bot search.");
                }
                if (botTask.getId() > 0) {
                    criteria.add(Restrictions.eq("id", botTask.getId()));
                } else if (botTask.getBot() != null && botTask.getName() != null) {
                    criteria.add(Restrictions.eq("bot", botTask.getBot()));
                    criteria.add(Restrictions.eq("name", botTask.getName()));
                } else {
                    throw new InternalApplicationException("BotTask should specify id or bot.name && name");
                }
                return (BotTask) criteria.uniqueResult();
            }
        });
    }

    @Override
    public List<BotStation> getBotStationList() {
        return getHibernateTemplate().loadAll(BotStation.class);
    }

    @Override
    public List<Bot> getBotList(final BotStation botStation) {
        return getHibernateTemplate().executeFind(new HibernateCallback<List<Bot>>() {

            @Override
            public List<Bot> doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(Bot.class);
                criteria.add(Restrictions.eq("botStation", botStation));
                return criteria.list();
            }
        });
    }

    @Override
    public List<Bot> getBotList(final Bot bot) {
        return getHibernateTemplate().executeFind(new HibernateCallback<List<Bot>>() {

            @Override
            public List<Bot> doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(Bot.class);
                if (bot.getId() <= 0 && Strings.isNullOrEmpty(bot.getWfeUser()) && bot.getBotStation() == null) {
                    throw new InternalApplicationException("Not enough parameters for Bot search.");
                }
                if (bot.getId() > 0) {
                    criteria.add(Restrictions.eq("id", bot.getId()));
                }
                if (bot.getWfeUser() != null) {
                    criteria.add(Restrictions.eq("wfeUser", bot.getWfeUser()));
                }
                if (bot.getBotStation() != null) {
                    criteria.add(Restrictions.eq("botStation", bot.getBotStation()));
                }
                return criteria.list();
            }
        });
    }

    @Override
    public List<BotTask> getBotTaskList(final Bot bot) {
        return getHibernateTemplate().executeFind(new HibernateCallback<List<BotTask>>() {

            @Override
            public List<BotTask> doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(BotTask.class);
                criteria.add(Restrictions.eq("bot", bot));
                return criteria.list();
            }
        });
    }

    @Override
    public BotStation create(BotStation bs) {
        getHibernateTemplate().save(bs);
        return bs;
    }

    @Override
    public Bot create(Bot bot) {
        getHibernateTemplate().save(bot);
        return bot;
    }

    @Override
    public BotTask create(BotTask task) {
        getHibernateTemplate().save(task);
        return task;
    }

    @Override
    public BotStation update(BotStation bs) {
        getHibernateTemplate().update(bs);
        return bs;
    }

    @Override
    public Bot update(Bot bot) {
        getHibernateTemplate().update(bot);
        return bot;
    }

    @Override
    public BotTask update(BotTask task) {
        if (task.getConfiguration() != null && task.getConfiguration().length == 0) {
            BotTask botTaskFromDB = new BotTask();
            botTaskFromDB.setId(task.getId());
            botTaskFromDB = getBotTask(botTaskFromDB);
            task.setConfiguration(botTaskFromDB.getConfiguration());
            getHibernateTemplate().evict(botTaskFromDB);
        }
        getHibernateTemplate().update(task);
        return task;
    }

    @Override
    public void remove(BotStation bs) {
        getHibernateTemplate().delete(bs);
    }

    @Override
    public void remove(Bot b) {
        getHibernateTemplate().delete(b);
    }

    @Override
    public void remove(BotTask task) {
        getHibernateTemplate().delete(task);
    }

}
