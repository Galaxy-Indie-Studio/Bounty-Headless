CREATE TABLE IF NOT EXISTS `bounties` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `issuer` varchar(16) NOT NULL,
  `hunted` varchar(16) NOT NULL,
  `reward` double NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `hunter` varchar(16) DEFAULT NULL,
  `turnedin` timestamp NULL DEFAULT NULL,
  `redeemed` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;
