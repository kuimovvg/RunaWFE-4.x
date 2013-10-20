function parserTimeBySecond(seconds, pattern) {
  var objDate = {
    seconds: Math.floor(seconds % 60),
    minutes: Math.floor(seconds / 60 % 60),
    hours: Math.floor(seconds / 3600 % 24),
    days: Math.floor(seconds / 86400)
  };

  return pattern.replace("SS", timerTagAddZeroPrefix(objDate.seconds))
      .replace("MM", timerTagAddZeroPrefix(objDate.minutes))
      .replace("HH", timerTagAddZeroPrefix(objDate.hours))
      .replace("DD", timerTagAddZeroPrefix(objDate.days));
}

function timerTagAddZeroPrefix(value) {
  if (value) {
    var str = value.toString();
    if (str.length == 1) {
      return "0" + str;
    }
    else if (str.length == 0) {
      return "00";
    }
    return str;
  }
  return "00";
}

