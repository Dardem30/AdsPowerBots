ALTER view vw_balance_overview as
    select va.id                                                                                 AS vk_id,
           a.id                                                                                  AS serf_id,
           va.browser_profile                                                                    AS browser_profile,
           va.proxy                                                                              AS proxy,
           ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0) - ifnull(log.balance, 0) + ifnull(a.balance_target, 0) -
           ifnull(log.balance_vktarget, 0) + ifnull(serf_ih.sum, 0) + ifnull(vlike_ih.sum, 0) +
           ifnull(tar_ih.sum, 0)
                                                                                                 AS todays_income,
           IF(ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0) - ifnull(log.balance, 0) + ifnull(serf_ih.sum, 0) +
              ifnull(vlike_ih.sum, 0) > 16,
              ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0) - ifnull(log.balance, 0) + ifnull(serf_ih.sum, 0),
              ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0) - ifnull(log.balance, 0) + ifnull(serf_ih.sum, 0) +
              ifnull(vlike_ih.sum, 0))                                                           AS serf_vlike_inc,
           ifnull(pva.balance, 0) - ifnull(log.balance_payup_video, 0)                           AS pva_income,
           ifnull(a.balance, 0) - ifnull(log.balance_vkserfing, 0) + ifnull(serf_ih.sum, 0)      as serf_inc,
           ifnull(a.balance_target, 0) - ifnull(log.balance_vktarget, 0) + ifnull(tar_ih.sum, 0) AS tar_inc,
           ifnull(a.balance_vlike, 0) - ifnull(log.balance_vlike, 0) + ifnull(vlike_ih.sum, 0)   AS vlike_inc,
           ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0)                                     AS serf_vlike,
           ifnull(a.balance, 0)                                                                  AS serf,
           ifnull(a.balance_target, 0)                                                           AS tar,
           ifnull(a.balance_vlike, 0)                                                            AS vlike,
           a.lastprocessed                                                                       AS lastprocessed,
           log.date,
           ifnull(pva.balance, 0)                                                                AS pva,
           ifnull(pva.captchas, 0)                                                                AS pva_captchas,
           ifnull(pva.captchas, 0) - ifnull(log.captchas, 0) AS solved_captchas
    from vktarget.vkserfing_account a
             join vktarget.vk_account va on a.id = va.vkserfing_account_id
             join vktarget.payeer_account pa on pa.vk_acc_id = va.id
             left join payup_video_account pva ON pva.vk_acc_id = va.id
             LEFT JOIN log_income log ON log.vk_id = va.id AND log.date = CURDATE() - INTERVAL 1 DAY
             LEFT JOIN invoice_history serf_ih
                       on va.id = serf_ih.vk_id AND serf_ih.date = CURDATE() - INTERVAL 0 DAY AND
                          serf_ih.service = 'vkserfing'
             LEFT JOIN invoice_history vlike_ih
                       on va.id = vlike_ih.vk_id AND vlike_ih.date = CURDATE() - INTERVAL 0 DAY AND
                          vlike_ih.service = 'vlike'
             LEFT JOIN invoice_history tar_ih on va.id = tar_ih.vk_id AND tar_ih.date = CURDATE() - INTERVAL 0 DAY AND
                                                 tar_ih.service = 'vktarget'
    WHERE ((va.blocked = '0' OR va.blocked is null) AND a.requires_verification is null)
    GROUP BY va.id, a.id, va.browser_profile, va.proxy, ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0) - ifnull(log.balance, 0) + ifnull(a.balance_target, 0) -
           ifnull(log.balance_vktarget, 0) + ifnull(serf_ih.sum, 0) + ifnull(vlike_ih.sum, 0) +
           ifnull(tar_ih.sum, 0), IF(ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0) - ifnull(log.balance, 0) + ifnull(serf_ih.sum, 0) +
              ifnull(vlike_ih.sum, 0) > 16,
              ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0) - ifnull(log.balance, 0) + ifnull(serf_ih.sum, 0),
              ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0) - ifnull(log.balance, 0) + ifnull(serf_ih.sum, 0) +
              ifnull(vlike_ih.sum, 0)), ifnull(pva.balance, 0) - ifnull(log.balance_payup_video, 0), ifnull(a.balance, 0) - ifnull(log.balance_vkserfing, 0) + ifnull(serf_ih.sum, 0), ifnull(a.balance_target, 0) - ifnull(log.balance_vktarget, 0) + ifnull(tar_ih.sum, 0), ifnull(a.balance_vlike, 0) - ifnull(log.balance_vlike, 0) + ifnull(vlike_ih.sum, 0), ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0), ifnull(a.balance, 0), ifnull(a.balance_target, 0), ifnull(a.balance_vlike, 0), a.lastprocessed, log.date, ifnull(pva.balance, 0), ifnull(pva.captchas, 0), ifnull(pva.captchas, 0) - ifnull(log.captchas, 0)


