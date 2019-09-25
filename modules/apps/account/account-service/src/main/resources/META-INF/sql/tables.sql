create table AccountEntry (
	mvccVersion LONG default 0 not null,
	accountEntryId LONG not null primary key,
	companyId LONG,
	userId LONG,
	userName VARCHAR(75) null,
	createDate DATE null,
	modifiedDate DATE null,
	parentAccountEntryId LONG,
	name VARCHAR(100) null,
	description STRING null,
	logoId LONG,
	status INTEGER
);

create table AccountEntryUserRel (
	mvccVersion LONG default 0 not null,
	accountEntryUserRelId LONG not null primary key,
	accountEntryId LONG,
	accountUserId LONG
);