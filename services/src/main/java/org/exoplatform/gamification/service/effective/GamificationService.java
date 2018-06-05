package org.exoplatform.gamification.service.effective;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.gamification.entities.domain.effective.GamificationContextEntity;
import org.exoplatform.gamification.entities.domain.effective.GamificationContextItemEntity;
import org.exoplatform.gamification.service.dto.effective.GamificationContextHolder;
import org.exoplatform.gamification.service.dto.effective.UserBadge;
import org.exoplatform.gamification.storage.dao.GamificationDAO;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.naming.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GamificationService {

    private static final Log LOG = ExoLogger.getLogger(GamificationService.class);

    protected final GamificationDAO gamificationDAO;

    public GamificationService() {
        this.gamificationDAO = CommonsUtils.getService(GamificationDAO.class);
    }

    /**
     * Find a GamificationContext by username
     *
     * @param username : gamification's username param
     * @return an instance of GamificationContextDTO
     */
    public GamificationContextEntity findGamificationContextByUername(String username) {

        GamificationContextEntity entity = null;

        try {
            //--- Get Entity from DB
            entity = gamificationDAO.findGamificationContextByUsername(username);


        } catch (Exception e) {
            LOG.error("Error to find Gamification entity with username : {}", username, e.getMessage());
        }
        return entity;

    }

    /**
     * Add GamificationContext to DB
     *
     * @param gamificationContextHolder : GamificationContext to be saved
     */
    @ExoTransactional
    public void saveGamificationContext(GamificationContextHolder gamificationContextHolder) {

        try {

            if (gamificationContextHolder.isNew()) {

                gamificationDAO.create(gamificationContextHolder.getGamificationContextEntity());

            } else {

                gamificationDAO.update(gamificationContextHolder.getGamificationContextEntity());

            }

        } catch (Exception e) {
            LOG.error("Error to save gamificationContext for user {}", gamificationContextHolder.getGamificationContextEntity().getUsername(), e);
        }
    }

    /**
     * @param username
     * @return
     */
    @ExoTransactional
    public long getUserGlobalScore(String username) {

        long userScore = 0;

        GamificationContextEntity entity = null;

        try {
            //--- Get Entity from DB
            entity = gamificationDAO.getUserGlobalScore(username);


        } catch (Exception e) {
            LOG.error("Error to find Gamification entity with username : {}", username, e.getMessage());
        }

        if (entity != null) {
            userScore = entity.getScore();
        }

        return userScore;
    }

    /**
     * @param gamificationSearch
     * @return
     */
    @ExoTransactional
    public List<GamificationContextEntity> filter(GamificationSearch gamificationSearch) {

        List<GamificationContextEntity> gamificationContextEntities = null;

        try {
            switch (gamificationSearch.getZone()) {
                case "social":
                    //Load Effective Gamification by rank
                    gamificationContextEntities = gamificationDAO.findLeaderboardByZone(gamificationSearch.getZone());
                    break;
                default:
                    //Load Effective Gamification by rank
                    gamificationContextEntities = gamificationDAO.findDefaultLeaderboard();
                    break;
            }


        } catch (Exception e) {
            LOG.error("Error to filter leaderboards by {} and {}", gamificationSearch.getFilter(), gamificationSearch.getZone(), e);
        }

        return gamificationContextEntities;
    }

    @ExoTransactional
    public Set<GamificationContextItemEntity> getUserGamification(String userId) {

        Set<GamificationContextItemEntity> gamificationContextItemEntitySet = null;

        try {
            //--- Get Entity from DB
            GamificationContextEntity gamificationContextEntity = gamificationDAO.getUserGamification(userId);

            gamificationContextItemEntitySet = gamificationContextEntity.getGamificationItems();


        } catch (Exception e) {
            LOG.error("Error to load effective gamification for user {} ", userId, e);
        }


        return gamificationContextItemEntitySet;
    }


}
