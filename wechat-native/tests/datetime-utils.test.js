const test = require('node:test')
const assert = require('node:assert/strict')

test('datetime utilities should split and merge picker values', () => {
  const { splitDateTimeValue, mergeDateTimeValue, formatPickerDateTimeLabel } = require('../utils/datetime')

  assert.deepEqual(splitDateTimeValue('2026-03-21T09:30:00'), {
    date: '2026-03-21',
    time: '09:30'
  })
  assert.deepEqual(splitDateTimeValue(''), {
    date: '',
    time: ''
  })
  assert.equal(mergeDateTimeValue('2026-03-21', '09:30'), '2026-03-21T09:30:00')
  assert.equal(formatPickerDateTimeLabel('2026-03-21', '09:30'), '2026-03-21 09:30')
  assert.equal(formatPickerDateTimeLabel('', ''), '请选择日期和时间')
})
