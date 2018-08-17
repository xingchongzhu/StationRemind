/*insert into station(lineid,pm,cname,pname,aname,lot,lat,stationinfo,transfer)
 select 11,1,stationName,PinYin,PinYinInitial,stationLat,stationLong,"06:34/23:26|06:35/23:41",
 0 from stations
*/

/*insert into exitinfo(cname,exitname,addr) select stationName,exitld,*/

/*delete from station*/

/*insert into exitinfo(cname,exitname,addr)select stations.stationName,
EXITS.exitName,POIS.poiName
*/

/*insert into exitinfo(cname,exitname,addr) select stations.stationName,
EXITS.exitName,POIS.poiName from EXITSPOIs join EXITS on  EXITSPOIs.exitId=EXITS.exitId
join POIS on POIS.poiId=EXITSPOIs.poiId join stations on stations.stationId = EXITS.stationId
*/


/*select cname,exitname,addr=
        stuff((select ','+addr from  exitinfo
            where c.cname=cname and c.exitname=exitname )from exitinfo c
*/
SELECT cname,exitname,STUFF((SELECT ',' + UserName FROM exitinfo WHERE cname=A.cname and
exitname=A.exitname For XML PATH('')),1, 1, '') AS A
FROM exitinfo A

