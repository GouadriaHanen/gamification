package org.exoplatform.addons.gamification.service.setting.rule;

import org.exoplatform.addons.gamification.service.setting.rule.model.RuleConfig;

public interface RuleRegistry {

    void addPlugin(RuleConfig rule);


    boolean remove(RuleConfig rule);
}
