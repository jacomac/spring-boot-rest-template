<!DOCTYPE html>
<html lang="de">
<head>
  <meta http-equiv="content-type" content="text/html;charset=utf-8"/>
  <title>Passwort zurück setzen</title>
</head>
<body>
  <p>Guten Tag ${name},</p>
  <p>Es wurde ein Link angefordert, mit dem Sie Ihr Passwort für das Sprest Nutzerkonto ${loginName} neu setzen können (Email ${email}).</p>
  <p>Der Link lautet wie folgt:</p>
  <p><a href="${baseUrl}/#/password-reset?token=${token}">${baseUrl}/#/password-reset?token=${token}</a></p>
  <p>Dieser Link verliert nach 24 Stunden seine Gültigkeit. Solange sie obigen Link nicht benutzen, 
  um Ihr Passwort zurück zu setzen, bleibt ihr jetziges erhalten.</p>
</body>
</html>
