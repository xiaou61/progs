function padNumber(value) {
  return String(value).padStart(2, '0')
}

function splitDateTimeValue(value) {
  if (!value) {
    return {
      date: '',
      time: ''
    }
  }

  const text = String(value)
  const [datePart = '', timePart = ''] = text.split('T')
  return {
    date: datePart.slice(0, 10),
    time: timePart.slice(0, 5)
  }
}

function mergeDateTimeValue(date, time) {
  if (!date || !time) {
    return ''
  }
  return `${date}T${time}:00`
}

function formatPickerDateTimeLabel(date, time) {
  if (!date && !time) {
    return '请选择日期和时间'
  }
  if (!date) {
    return `已选择时间 ${time}`
  }
  if (!time) {
    return `已选择日期 ${date}`
  }
  return `${date} ${time}`
}

function buildDefaultPickerValue(now = new Date()) {
  return {
    date: `${now.getFullYear()}-${padNumber(now.getMonth() + 1)}-${padNumber(now.getDate())}`,
    time: `${padNumber(now.getHours())}:${padNumber(now.getMinutes())}`
  }
}

module.exports = {
  buildDefaultPickerValue,
  formatPickerDateTimeLabel,
  mergeDateTimeValue,
  splitDateTimeValue
}
