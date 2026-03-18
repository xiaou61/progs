function formatDateTime(value) {
  if (!value) {
    return ''
  }
  return String(value).replace('T', ' ').slice(0, 16)
}

function formatDateTimeRange(startAt, endAt) {
  return `${formatDateTime(startAt)} - ${formatDateTime(endAt)}`
}

function toPositiveNumber(value, fallbackValue) {
  const nextValue = Number(value)
  return Number.isFinite(nextValue) && nextValue > 0 ? nextValue : fallbackValue
}

module.exports = {
  formatDateTime,
  formatDateTimeRange,
  toPositiveNumber
}
