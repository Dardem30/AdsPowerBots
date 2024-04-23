SELECT va.phone, va.password, a.email, a.password FROM vlike_account a
INNER JOIN vk_account va ON va.vlike_account_id = a.id
WHERE a.cookie is null