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
package ru.runa.wfe.bot.dao;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.commons.dao.CommonDAO;

import com.google.common.base.Strings;

/**
 * DAO level interface for managing bot stations and so on.
 * @author Konstantinov Aleksey 25.02.2012
 */
@SuppressWarnings("unchecked")
public class BotDAO extends CommonDAO {

    /**
     * Load {@linkplain BotStation} from database.
     * Loading performed by identity and name, provided by parameter.
     * Throws {@linkplain InternalApplicationException} if no identity and name found. 
     * @param botStation {@linkplain BotStation}, which parameter used to load result.
     * @return Loaded {@linkplain BotStation} or null, if no bot station found.
     */
    public BotStation getBotStation(final BotStation botStation) {
        return getHibernateTemplate().execute(new HibernateCallback<BotStation>() {

            @Override
            public BotStation doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(BotStation.class);
                if (botStation.getId() == null && Strings.isNullOrEmpty(botStation.getName())) {
                    throw new InternalApplicationException("Not enough parameters for BotStation search.");
                }
                if (botStation.getId() != null) {
                    criteria.add(Restrictions.eq("id", botStation.getId()));
                }
                if (!Strings.isNullOrEmpty(botStation.getName())) {
                    criteria.add(Restrictions.eq("name", botStation.getName()));
                }
                return (BotStation) criteria.uniqueResult();
            }
        });
    }

    /**
     * Load {@linkplain Bot} from database.
     * Loading performed by identity and userName and bot station, provided by parameter.
     * Throws {@linkplain InternalApplicationException} if no identity and userName and bot station found. 
     * @param bot {@linkplain Bot}, which parameter used to load result.
     * @return Loaded {@linkplain Bot} or null, if no bot found.
     */
    public Bot getBot(final Bot bot) {
        return getHibernateTemplate().execute(new HibernateCallback<Bot>() {

            @Override
            public Bot doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(Bot.class);
                if (bot.getId() == null && Strings.isNullOrEmpty(bot.getWfeUser()) && bot.getBotStation() == null) {
                    throw new InternalApplicationException("Not enough parameters for BotRunner search.");
                }
                if (bot.getId() != null) {
                    criteria.add(Restrictions.eq("id", bot.getId()));
                } else if (bot.getWfeUser() != null) {
                    criteria.add(Restrictions.eq("wfeUser", bot.getWfeUser()));
                } else if (bot.getBotStation() != null) {
                    criteria.add(Restrictions.eq("botStation", bot.getBotStation()));
                } else {
                    throw new InternalApplicationException("BotRunner should specify id or wfeUser or botStation");
                }
                return (Bot) criteria.uniqueResult();
            }
        });
    }

    /**
     * Load {@linkplain BotTask} from database.
     * Loading performed by identity and taskName and bot, provided by parameter.
     * Throws {@linkplain InternalApplicationException} if no identity and taskName and bot found. 
     * @param bot {@linkplain BotTask}, which parameter used to load result.
     * @return Loaded {@linkplain BotTask} or null, if no bot task found.
     */
    public BotTask getBotTask(final BotTask botTask) {
        return getHibernateTemplate().execute(new HibernateCallback<BotTask>() {

            @Override
            public BotTask doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(BotTask.class);
                if (botTask.getId() == null && botTask.getBot() == null && botTask.getName() == null) {
                    throw new InternalApplicationException("Not enough parameters for BotRunner search.");
                }
                if (botTask.getId() != null) {
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

    /**
     * Load all {@linkplain BotStation}'s from database. 
     * @return {@linkplain BotStation}'s list.
     */
    public List<BotStation> getBotStationList() {
        return getHibernateTemplate().loadAll(BotStation.class);
    }

    /**
     * Load all {@linkplain Bot}'s, defined for {@linkplain BotStation}.
     * @param botStation {@linkplain BotStation} to load {@linkplain Bot}'s. 
     * @return {@linkplain Bot}'s, defined for {@linkplain BotStation}.
     */
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

    /**
     * Load {@linkplain Bot}'s.
     * Loading performed by identity and userName and bot station, provided by parameter.
     * @param bot {@linkplain Bot}, which parameter used to load result.
     * @return {@linkplain Bot}'s list.
     */
	public List<Bot> getBotList(final Bot bot) {
        return getHibernateTemplate().executeFind(new HibernateCallback<List<Bot>>() {

            @Override
            public List<Bot> doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(Bot.class);
                if (bot.getId() == null && Strings.isNullOrEmpty(bot.getWfeUser()) && bot.getBotStation() == null) {
                    throw new InternalApplicationException("Not enough parameters for BotRunner search.");
                }
                if (bot.getId() != null) {
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

    /**
     * Load all {@linkplain BotTask}, defined for {@linkplain Bot}.
     * @param bot {@linkplain Bot} to load {@linkplain BotTask}'s.
     * @return {@linkplain BotTask}, defined for {@linkplain Bot}.
     */
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

    /**
     * Save {@linkplain BotStation}. {@linkplain BotStation} must not be saved before.
     * @param botStation {@linkplain BotStation} to save.
     * @return Saved {@linkplain BotStation}.
     */
    public BotStation create(BotStation bs) {
        getHibernateTemplate().save(bs);
        return bs;
    }

    /**
     * Save {@linkplain Bot}. {@linkplain Bot} must not be saved before.
     * @param bot {@linkplain Bot} to save.
     * @return Saved {@linkplain Bot}.
     */
    public Bot create(Bot bot) {
        getHibernateTemplate().save(bot);
        return bot;
    }

    /**
     * Save {@linkplain BotTask}. {@linkplain BotTask} must not be saved before. 
     * @param botTask {@linkplain BotTask} to save.
     * @return Saved {@linkplain BotTask}.
     */
    public BotTask create(BotTask task) {
        getHibernateTemplate().save(task);
        return task;
    }

    /**
     * Update {@linkplain BotStation}. {@linkplain BotStation} must be saved before.
     * @param botStation {@linkplain BotStation} to update.
     * @return Updated {@linkplain BotStation}.
     */
    public BotStation update(BotStation bs) {
        getHibernateTemplate().update(bs);
        return bs;
    }

    /**
     * Update {@linkplain Bot}. {@linkplain Bot} must be saved before.
     * @param bot {@linkplain Bot} to update.
     * @return Updated {@linkplain Bot}.
     */
    public Bot update(Bot bot) {
        getHibernateTemplate().update(bot);
        return bot;
    }

    /**
     * Update {@linkplain BotTask}. {@linkplain BotTask} must be saved before. 
     * @param botTask {@linkplain BotTask} to update.
     * @return Updated {@linkplain BotTask}.
     */
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

    /**
     * Remove saved {@linkplain BotStation}.
     * @param botStation {@linkplain BotStation} to remove.
     */
    public void remove(BotStation bs) {
        getHibernateTemplate().delete(bs);
    }

    /**
     * Remove saved {@linkplain Bot}.
     * @param bot {@linkplain Bot} to remove.
     */
    public void remove(Bot b) {
        getHibernateTemplate().delete(b);
    }

    /**
     * Remove saved {@linkplain BotTask}. 
     * @param botTask {@linkplain BotTask} to remove.
     */
    public void remove(BotTask task) {
        getHibernateTemplate().delete(task);
    }

}
