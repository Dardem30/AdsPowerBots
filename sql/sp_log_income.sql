DROP PROCEDURE IF EXISTS sp_log_income;
CREATE PROCEDURE sp_log_income(dayIndex int)
BEGIN
    INSERT INTO log_income(vk_id, income, vkserfing_income, vktarget_income, vlike_income,
                           balance, balance_vkserfing, balance_vktarget, balance_vlike,
                           date, balance_payup_video, payup_video_income,
                           captchas, solved_captchas)
    SELECT b.vk_id,
           IF (b.todays_income > b.serf_vlike_inc AND b.todays_income < 20, b.todays_income, b.serf_vlike_inc),
           b.serf_inc,
           b.tar_inc,
           b.vlike_inc,
           b.serf_vlike,
           b.serf AS serf,
           b.tar AS tar,
           b.vlike AS vlike,
           CURDATE() - INTERVAL 1 - 1 DAY,
           b.pva AS pva,
           b.pva_income AS pva_income,
           b.pva_captchas,
           b.solved_captchas
    FROM vw_balance_overview b ;

    INSERT INTO daily_income_history(date, sum, average_per_acc, sum_dollars, average_per_acc_dollars, spent_on_captcha)
    SELECT NOW(),
           SUM(income),
           SUM(income) / COUNT(*),
           SUM(payup_video_income),
           SUM(payup_video_income) / COUNT(*),
           SUM(solved_captchas * 0.07)
    FROM log_income WHERE income > 0 AND date = CURDATE() - INTERVAL dayIndex - 1 DAY;
END;

# DROP PROCEDURE IF EXISTS sp_log_income;
# CREATE PROCEDURE sp_log_income(dayIndex int)
# BEGIN
#     INSERT INTO log_income(vk_id, income, vkserfing_income, vktarget_income, vlike_income,
#                            balance, balance_vkserfing, balance_vktarget, balance_vlike, date, balance_payup_video, payup_video_income)
#     SELECT va.id,
#            IF(ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0) - ifnull(log.balance, 0) + ifnull(serf_ih.sum, 0) + ifnull(vlike_ih.sum, 0) > 16,
#               ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0) - ifnull(log.balance, 0) + ifnull(serf_ih.sum, 0),
#               ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0) - ifnull(log.balance, 0) + ifnull(serf_ih.sum, 0) + ifnull(vlike_ih.sum, 0)) AS serf_vlike_inc,
#            ifnull(a.balance, 0) - ifnull(log.balance_vkserfing, 0) + ifnull(serf_ih.sum, 0) as serf_inc,
#            ifnull(a.balance_target, 0) - ifnull(log.balance_vktarget, 0) AS tar_inc,
#            ifnull(a.balance_vlike, 0) - ifnull(log.balance_vlike, 0) + ifnull(vlike_ih.sum, 0) AS vlike_inc,
#            ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0) AS serf_vlike,
#            ifnull(a.balance, 0) AS serf,
#            ifnull(a.balance_target, 0) AS tar,
#            ifnull(a.balance_vlike, 0) AS vlike,
#            CURDATE() - INTERVAL dayIndex - 1 DAY,
#            ifnull(pva.balance, 0) AS pva,
#            ifnull(pva.balance, 0) - ifnull(log.balance_payup_video, 0) AS pva_income
#     FROM vkserfing_account a
#              INNER JOIN vk_account va on a.id = va.vkserfing_account_id
#              left join payup_video_account pva ON pva.vk_acc_id = va.id
#              LEFT JOIN log_income log ON log.vk_id = va.id AND log.date = CURDATE() - INTERVAL dayIndex DAY
#              LEFT JOIN invoice_history serf_ih on va.id = serf_ih.vk_id AND serf_ih.date = CURDATE() -  INTERVAL dayIndex - 1 DAY  AND serf_ih.service = 'vkserfing'
#              LEFT JOIN invoice_history vlike_ih on va.id = vlike_ih.vk_id AND vlike_ih.date = CURDATE() -  INTERVAL dayIndex - 1 DAY  AND vlike_ih.service = 'vlike'
#     WHERE ((va.blocked = '0' OR va.blocked is null) AND a.requires_verification is null)
#     GROUP BY va.id, IF(ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0) - ifnull(log.balance, 0) + ifnull(serf_ih.sum, 0) + ifnull(vlike_ih.sum, 0) > 16,
#               ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0) - ifnull(log.balance, 0) + ifnull(serf_ih.sum, 0),
#               ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0) - ifnull(log.balance, 0) + ifnull(serf_ih.sum, 0) + ifnull(vlike_ih.sum, 0)), ifnull(a.balance, 0) - ifnull(log.balance_vkserfing, 0) + ifnull(serf_ih.sum, 0), ifnull(a.balance_target, 0) - ifnull(log.balance_vktarget, 0), ifnull(a.balance_vlike, 0) - ifnull(log.balance_vlike, 0) + ifnull(vlike_ih.sum, 0), ifnull(a.balance, 0) + ifnull(a.balance_vlike, 0), ifnull(a.balance, 0), ifnull(a.balance_target, 0), ifnull(a.balance_vlike, 0), CURDATE() - INTERVAL dayIndex - 1 DAY, ifnull(pva.balance, 0), ifnull(pva.balance, 0) - ifnull(log.balance_payup_video, 0);
#
#     INSERT INTO daily_income_history(date, sum, average_per_acc, sum_dollars, average_per_acc_dollars)
#     SELECT NOW(), SUM(income), SUM(income) / COUNT(*), SUM(payup_video_income), SUM(payup_video_income) / COUNT(*) FROM log_income WHERE income > 0
#                                                                                                                                      AND date = CURDATE() - INTERVAL dayIndex - 1 DAY;
# END;