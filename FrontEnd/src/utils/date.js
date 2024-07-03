const gapMap = {
    'DAY': 1000*60*60*24,
    'HOUR': 1000*60*60,
    'MINUTE': 1000*60
}

export function getTimeStamp(milliseconds){
    let date=new Date(milliseconds)
    let ymd =  date.toLocaleDateString()
    const parts = ymd.split('/'); // 分割字符串成为数组 [YYYY, M, D]
    const year = parts[0];
    const month = parts[1].padStart(2, '0'); // 确保月份是两位数
    const day = parts[2].padStart(2, '0'); // 确保天数是两位数
    return `${year}-${month}-${day}`+ " " + date.toTimeString().substring(0, 8)
}

//2024-03-17 00:24:19 格式计算时间差
export function getTimeGap(timeStamp1, timeStamp2, unit){
    if (!gapMap[unit]){
        throw new Error('unit is not valid')
    }
    let date1 = new Date(timeStamp1)
    let date2 = new Date(timeStamp2)
    let gap = Math.abs(date2.getTime() - date1.getTime())
    return Math.floor(gap/gapMap[unit])
}