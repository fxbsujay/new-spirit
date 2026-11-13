export const TypeConstant = [
    { label: '实时对局', value: 'SHORT' },
    { label: '通讯棋', value: 'LONG' },
    { label: '无限制', value: 'NONE' }
]

export const Regex = {
    PASSWORD: /^[a-zA-Z0-9@!$^.*_%]{6,30}$/,
    USERNAME: /^[A-Za-z][a-zA-Z0-9]{1,19}/,
    EMAIL_CODE: /^[A-Z0-9]{5}$/,
    EMAIL: /.+@.+\..+/,
}

export const GameReason = [
    { label: '超时', value: 'TIMEOUT' },
    { label: '认输', value: 'SURRENDER' },
    { label: '胜出', value: 'BEAT' },
    { label: '和棋', value: 'PEACE' },
    { label: '取消', value: 'CANCEL' }
]

export const getGameEnding = (winner, reason) => {
    let text = ''
    if (reason === 'PEACE') {
        text = '双方和棋'
    } else if (reason === 'CANCEL') {
        text = winner === 'WHITE' ? '黑棋取消对局' : '白棋取消对局'
    }  else if (reason === 'BEAT') {
        text = winner === 'WHITE' ? '执白棋胜' : '执黑棋胜'
    } else if (reason === 'SURRENDER') {
        text = winner === 'WHITE' ? '黑棋认输 • 执白棋胜' : '白棋认输 • 执黑棋胜'
    } else if (reason === 'TIMEOUT') {
        text = winner === 'WHITE' ? '黑棋超时 • 执白棋胜' : '白棋超时 • 执黑棋胜'
    }

    return text
}