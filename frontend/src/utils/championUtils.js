export const getChampionImageSrc = (championName) => {
    if (championName && championName.startsWith('basic_')) {
        return `/portrait/${championName}.png`;
    }

    const specialCases = {
        'missfortune': 'MissFortune',
        'drmundo': 'DrMundo',
        'jarvaniv': 'JarvanIV',
        'leesin': 'LeeSin',
        'masteryi': 'MasterYi',
        'tahmkench': 'TahmKench',
        'twistedfate': 'TwistedFate',
        'xinzhao': 'XinZhao',
        'aurelionsol': 'AurelionSol',
        'kogmaw': 'KogMaw',
        'reksai': 'RekSai',
        'ksante': 'KSante'
    };

    // 소문자 변환 후 특수 케이스 확인
    const lowerName = championName?.toLowerCase() || '';
    const formattedName = specialCases[lowerName] || (championName?.charAt(0).toUpperCase() + championName?.slice(1).toLowerCase());

    return `/champions/${formattedName || 'Default'}.png`;
};
