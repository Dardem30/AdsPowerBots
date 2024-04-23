CREATE TABLE account
(
    id       INT(11) AUTO_INCREMENT NOT NULL,
    email    varchar(255),
    password varchar(255),
    cookie   text,
    PRIMARY KEY (id)
);
CREATE TABLE vk_account
(
    id         INT(11) AUTO_INCREMENT NOT NULL,
    phone      varchar(255),
    login      varchar(255),
    password   varchar(255),
    cookie     text,
    account_id int(11),
    PRIMARY KEY (id)
);
INSERT INTO account(email, password, cookie)
VALUES ('r.lukashenko@mail.ru', 'jrt-wdp9edh2jcw@GCB',
        'PHPSESSID=1br3a6rq3f7tn9r46cjg1d5qtf; ref_page=https%3A%2F%2Fktonanovenkogo.ru%2F; ref=1918492; _ym_uid=1668369126366142434; _ym_d=1668369126; _ga=GA1.2.187656646.1668369126; _gid=GA1.2.2068586611.1668369126; _ym_isad=1; COPINY_AUTH=Nm5FSWVOMzVCZlZhODhuUytrQ2x3dz0');
INSERT INTO vk_account(phone, login, password, cookie, account_id)
VALUES ('+375 25 701-43-73', null, 'bfw_wqt2ujv4PBV2vqh',
        'remixscreen_depth=24; tmr_lvid=3d4b316c3008891759c26ef6899686df; tmr_lvidTS=1580722911037; remixscreen_width=2560; remixscreen_height=1440; remixstid=2100955510_l8IVLbvhtorJnEbX43YeDvZmcrjZhxaD1xlTlC02VuL; remixdt=0; remixcolor_scheme_mode=auto; remixdark_color_scheme=0; remixlang=0; remixluas2=ZDI1OTZiODUwYTU2NGQwYzUzZGU5YmI1; remixstlid=9092276026198767233_8fVJWcGQUPKz3ggKbxl8ZBzvZaCELy6Ph7RyEEnKhbg; remixflash=0.0.0; remixuas=YWI0MmRmYmUyMDI3MzI3MDM3ZjkyZDhi; remixscreen_orient=1; remixscreen_dpr=1; remixrefkey=37ab24be1b8b890a39; remixbdr=1; remixua=41%7C-1%7C194%7C3805075157; remixgp=3c0c7cab5d1b9592f18ed4e69f5c0cf3; remixsuc=1%3A; remixdmgr=31e1f0d37b8fba9b47ff86d2c3af266249bf6c9f1fd8c017161750d66aebc1e5; remixpuad=pUB2iE37riPNGYWJQTvwLlx3rnP9POOsjLSExc7boTg; remixnsid=vk1.a.z5K82c9Z57DY0F5xJWUIxaL69OUqEF5R9YUb81xlP4ov9CFj_NXkXfCEhpNYlHWNxuNLjp24yTB44XwyEl99G_DtMI-rpFr1uCW8tns_cg6n3-VIe0FOGsZAs56ERp1ZRwdqohJLarM66RZHnTh4UBieDZvezYrFGfCQ-6XLaBoviB_OqsXRFMjHz4_7gk-b; remixsid=1_TdSArVub4cBjN-Tsa-ZPoDYWAhaBYiC2T24SUfHiQSrSADN7VXq-nDLJGLA3CoO6ySvILG34AikHMdZV8BjE_A; remixscreen_winzoom=1.99; tmr_detect=1%7C1668405511457; tmr_reqNum=432',
        1);
CREATE TABLE youtube_account
(
    id            INT(11) AUTO_INCREMENT NOT NULL,
    login         varchar(255),
    password      varchar(255),
    authorization text,
    cookie        text,
    clientdata    text,
    visitorid     text,
    account_id    int(11),
    PRIMARY KEY (id)
);
INSERT INTO youtube_account(login, password, authorization, cookie, account_id, clientdata, visitorid)
VALUES ('r.lukashenko@mail.ru', '22062001lll',
        'SAPISIDHASH 1668425824_81ecfc5fac7e9702611300dad682f97684979864',
        'CONSENT=WP.287506; LOGIN_INFO=AFmmF2swRQIhAN9lmVF0xMzYkJXziNRIA7aiIYV1nkLuCwIfVtRW9r_xAiBpw44m-1-OrT1Uk0y9w_hhYrwMw9tasCKvVm0fRNnRgA:QUQ3MjNmeDVIZDJZcHZWVFVQU0lhQ1N0QzZaOFJTc1BlaWlFVnRkd3FrMmFDblpUT09LYlZ6RzFmVjVkOVQ1ZW8yN0h4UVd4ODFCc2hNZE9hQlpjY2RqMjhPdzk2dTE2LS1uU2FxREU2eURJZlVFZXhQYU1fWDZsQ01DMUswSkRkaE9KWnZEYTBjVnB3UHpTb1NBNFJ3QWExTWtVMnFhSnRB; VISITOR_INFO1_LIVE=1bz2ZH-6Phs; PREF=tz=Europe.Minsk&f6=400&f5=30000&f4=4000000&f7=150; SID=QgjBs-nQfBn9bvNSukUi0-uhbe9sz4Tau6xo0OB1bpugO0Zz97g3QuMxwwr1whI2V-Otfg.; __Secure-1PSID=QgjBs-nQfBn9bvNSukUi0-uhbe9sz4Tau6xo0OB1bpugO0ZzUxCq47ZLmjNvP7ZCkPNg_Q.; __Secure-3PSID=QgjBs-nQfBn9bvNSukUi0-uhbe9sz4Tau6xo0OB1bpugO0ZzufMxt9rEXJXWbyWsyH5GEg.; HSID=A1eZHEWm-dK1r5XcT; SSID=AoUH1GyomFn-FXwh5; APISID=TC9eIazPTUdfdqtd/ANrbqmpdEjTjBs_zQ; SAPISID=WbX10VeufNtz_Ni4/AXRvQ_E14E80tc9Xf; __Secure-1PAPISID=WbX10VeufNtz_Ni4/AXRvQ_E14E80tc9Xf; __Secure-3PAPISID=WbX10VeufNtz_Ni4/AXRvQ_E14E80tc9Xf; YSC=Y1FPq5NrQio; wide=1; CONSISTENCY=ADecB4tSIdlNjSF7fZ3XSoDKIeJhjapk9TPdwBO0dySuddGBYiseKeQg0-8ex4gGipOjKPfMundBRQuwlPUYheVDEmwTC-1_lszItmYEkvSfX3EpQYCsBQP64GM; SIDCC=AIKkIs3pejHNolaut6wB-7xclEHK_qzGtkH4VuNX4G5XqGWAFK9sCfTW9dIyT6rlzvwBqu0-W4Tv; __Secure-1PSIDCC=AIKkIs2df_jQGPV5iT0ejiqN023Fu0IMryrzZkudWnyREyOQS4ToeIVNDVMBQwWc13iA7htBNQ; __Secure-3PSIDCC=AIKkIs1DRxUXFWf3llF6inVfxOp2uDKu10v6hPz6usIcZ564PjwiXYeBIwBQWNPz5l_uIQ_yy04',
        1,
        'CKi1yQEIkLbJAQiitskBCMS2yQEIqZ3KAQjZ08oBCLrzygEIlaHLAQi1vMwBCJK9zAEIxuHMAQj56MwBCOXrzAEI8O3MAQj+7cwBCPPxzAEInvPMAQif88wBCI33zAEIl/fMAQiQ+MwB',
        'CgsxYnoyWkgtNlBocyjg0MibBg%3D%3D');
CREATE TABLE vlike_account
(
    id       INT(11) AUTO_INCREMENT NOT NULL,
    email    varchar(255),
    password varchar(255),
    cookie   text,
    PRIMARY KEY (id)
);
INSERT INTO vlike_account(email, password, cookie)
VALUES ('r.lukashenko@mail.ru', 'CGGvhy4M8fj5NWy',
        '_gid=GA1.2.291076927.1668506198; cf_chl_rc_m=11; cf_chl_2=6de94dde9763192; cf_chl_prog=x16; cf_clearance=azfadJWZ4uAn9QPOzpy68u5MQZlEEb6Gnx1UflOCx_A-1668506593-0-150; remember_web_59ba36addc2b2f9401580f014c7f58ea4e30989d=eyJpdiI6IjI4WmJmYWRNRk5xNnlXSGpRY05mK0E9PSIsInZhbHVlIjoiRy81TDdFZDJIRjZ3WE9PMUdZK0xJQkNKNGtCVnRWdCtqK3cwQW43Zng5YmR2WERwd0hTQUJITXZOOVBWR3NWYk1mSmoyRGl0d0ZVb3l3RkRkakpScUM4NG5YU2Rrbm94OVkzZkZyQ254ZlF0M2Nwa1o2bXVJMU9zVmJPdjJJRFFDMVByS3NnSzVtYVNOWUIvUThoN20rZDhFdm9hMTJpY2xKWENqZU9ycDZvPSIsIm1hYyI6ImZjODc3MzI3MTQyMDNmZGJjZDM1OTQ0MTNhMGM4Y2ZhY2NiY2Q4MDYzNjE4OGVhNzRkZTAwY2Q2OGU3YTA0NWEiLCJ0YWciOiIifQ%3D%3D; _ym_uid=1668506590388618672; _ym_d=1668506590; _ym_isad=2; _ym_visorc=w; __gads=ID=b53fa51e82e6eba8-224102b8b1ce0060:T=1668506604:RT=1668506604:S=ALNI_MZ2GIL2ha1waLog0HS-Gy-C0inTLw; __gpi=UID=00000b8196ec74c5:T=1668506604:RT=1668506604:S=ALNI_MZ-Z2A01fhx8m_Ew2csnq4ouoPV4Q; XSRF-TOKEN=eyJpdiI6IiszTWlyVkhGb2RKTDV4Uk1BQ2NkOEE9PSIsInZhbHVlIjoicXRxMXk4UWRhRGJ2NDl2SjZncWhrVnB3M2lzdDkyZnRzQ3h3MEZVMnpIRFV2MlY5N0t6cE8yOGU3RmVaYm13cGhtbDBhekh6aDQ2bkNtMUVTVUl2bjZjVWxHdWpkOGdudEd1UHpnd0NXakxZQTk1QjUyZU5mazBjQ0pRMlFyYlUiLCJtYWMiOiI1NWJlYWE4NzMzMWI3ODM3MjFkODJlNzQyYjE5YzJiYTYyNGY1YjMzNmU3OGU0MGE1NDU2MWE0MmVjMzhlZTQyIiwidGFnIjoiIn0%3D; v_likeru_session=eyJpdiI6ImlFS2dXQ2tUQ3RuR1R1WXdpbkZoK0E9PSIsInZhbHVlIjoicUIvc1ExN2E2N29sdlNLazdNbzdFSjI5ZEkwS1VYVGJEdW5xUExKcDhLVkE3Z2NlZEVMaE16MEJtOWg5Y2EwYmFsWGdxaVFiYzNWRzJ1NmxwZ1lDL0srOVl3eFNOVkMrUWc1YlQxZGx4UUlHcllpZ3EvV0wvcGo0QzV2SlFGNVUiLCJtYWMiOiJlNmYyZjEzZWZiMjhlM2I3MTg1YTQwNDhiZTc4ZThjNTlhOTMyOTAxZTU4ZWVmN2QxMjRlMzE3ZGFjNmIxMjNmIiwidGFnIjoiIn0%3D; _ga_PL8EF1TR5H=GS1.1.1668506198.1.1.1668507109.0.0.0; _ga=GA1.2.804460354.1668506198; _gat_gtag_UA_32885995_1=1');
ALTER TABLE vk_account
    ADD vlike_account_id int null;
ALTER TABLE youtube_account
    ADD vlike_account_id int null;
UPDATE vk_account
SET vlike_account_id = 1;
UPDATE youtube_account
SET vlike_account_id = 1;
ALTER TABLE vk_account
    ADD access_token varchar(500) null;
ALTER TABLE vk_account
    ADD userid varchar(500) null;
CREATE TABLE vk_account_cookies
(
    id            int(11) AUTO_INCREMENT NOT NULL,
    name          varchar(255)           not null,
    value         varchar(1000)          null,
    vk_account_id int                    not null,
    PRIMARY KEY (id)
);
ALTER TABLE vlike_account
    ADD balance float null;
ALTER TABLE account
    ADD balance float null;
CREATE TABLE blacklist
(
    id   int(11) AUTO_INCREMENT NOT NULL,
    link varchar(500),
    PRIMARY KEY (id)
);
CREATE INDEX ix_link ON blacklist(link);
CREATE TABLE vkserfing_account
(
    id       INT(11) AUTO_INCREMENT NOT NULL,
    email    varchar(255),
    password varchar(255),
    cookie   text,
    PRIMARY KEY (id)
);
ALTER TABLE vk_account ADD vkserfing_account_id int null;
CREATE TABLE ipweb_account
(
    id       INT(11) AUTO_INCREMENT NOT NULL,
    email    varchar(255),
    password varchar(255),
    cookie   text,
    PRIMARY KEY (id)
);
ALTER TABLE ipweb_account ADD login varchar(255) null ;
ALTER TABLE vk_account ADD ipweb_account_id int null;
ALTER TABLE ipweb_account
    ADD balance float null;


ALTER TABLE vkserfing_account
    ADD balance float null;

ALTER TABLE vkserfing_account
    ADD cooldown timestamp null;
ALTER TABLE vkserfing_account
    ADD xcsrftoken varchar(500) null;

ALTER TABLE vk_account ADD cantaddfriends boolean not null default false;
ALTER TABLE vk_account ADD cantaddfriendssincewhen datetime null;
ALTER TABLE vk_account ADD cantsubscribe boolean not null default false;
ALTER TABLE vk_account ADD cantsibscribesincewhen datetime null;
ALTER TABLE vk_account ADD proxy varchar(255) null;
ALTER TABLE vk_account ADD port varchar(255) null;
ALTER TABLE vk_account ADD proxy_username varchar(255) null;
ALTER TABLE vk_account ADD proxy_password varchar(255) null;
ALTER TABLE account ADD errors varchar(1000) null;
ALTER TABLE vk_account ADD available_subscribes int null;
ALTER TABLE vk_account ADD available_friend_requests int null;
ALTER TABLE vk_account ADD available_shares int null;
CREATE TABLE restricted_keywords(
    keyword varchar(255) NOT NULL,
    PRIMARY KEY(keyword)
);

ALTER TABLE account
    ADD cooldown timestamp null;
ALTER TABLE vlike_account
    ADD cooldown timestamp null;

CREATE TABLE payeer_account
(
    id       INT(11) AUTO_INCREMENT NOT NULL,
    email    varchar(255),
    password varchar(255),
    secret_code varchar(255),
    acc_name varchar(255),
    vk_acc_id int(11) not null,
    PRIMARY KEY (id)
);
ALTER TABLE vkserfing_account ADD requires_verification boolean null ;
ALTER TABLE payeer_account ADD cookie varchar(255) null;
ALTER TABLE payeer_account ADD session_id varchar(255) null;
ALTER TABLE payeer_account ADD balance float null;
ALTER TABLE payeer_account ADD invoice boolean null;
ALTER TABLE vlike_account ADD invoice boolean null;
ALTER TABLE vkserfing_account ADD invoice boolean null;
ALTER TABLE payeer_account ADD disabled boolean null;
ALTER TABLE vk_account ADD browser_profile varchar(255) null;
ALTER TABLE vkserfing_account ADD balance_target float null;
ALTER TABLE vkserfing_account ADD balance_vlike float null;
ALTER TABLE vkserfing_account
    ADD lastprocessed timestamp null;
ALTER TABLE vkserfing_account
    ADD vktarget_invoice_verified char null ;

ALTER TABLE vk_account ADD blocked char(1) null;
ALTER TABLE vk_account ADD unblock_applied char(1) null;
ALTER TABLE vkserfing_account ADD target_invoiced char(1) null;
ALTER TABLE vk_account ADD reserve_codes varchar(500) null;