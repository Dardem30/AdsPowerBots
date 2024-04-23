# AdsPowerBots
Leaving this project here for historical purpose in memory for what i did in 2023

This project has integration with:
1. https://sms-activate.org/ - token is hardcoded and the account has some balance btw ^_^ (SmsActivate.java)
2. https://5sim.net/ - same as 1 (FiveSimService.java)
3. https://proxy6.net/en/ - for maintaining proxies (Proxy6Client.java)
4. https://2captcha.com - for breaking captchas (RuCaptcha.java)

Entry points:
1. AdsPowerService.java - used for processing tasks for bots to earn money
2. ForgeAccountSmsActivate.java - used to forge new bots
3. AdsPowerPayeerWithdraw.java - used to withdraw balance from bots(which are marked to be invoiced) to central payeer account 
4. RandomResolutionGenerator.java - used to generate random resolution per bot

Activities:
1. For day time bots were executing tasks on services vkserfing, vktarget, vlike - yandex metric wasn't able to determine the bots due to random and "drank" behaviour of bots
2. For night time bots were watching youtube videos on payup.video service - here i had an interesting task to break captchas which as turned out also wasn't a big issue(see method AdsPowerService.solvePayUpCaptcha)
