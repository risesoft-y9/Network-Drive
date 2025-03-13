/**
 * 判断是否是移动端
 * @returns {Boolean}
 * @author Yehaifeng
 */
export const isMobile = (): Boolean => {
    return /Android|webOS| iPhone | iPad | iPod |BlackBerry|opera mini|opera mobile|appleWebkit.*mobile|mobile/i.test(
        navigator.userAgent
    );
};

/**
 * 根据key（必须为数字）值排序
 * 使用示例 array.sort(compare("key"))
 * @returns {Array}
 * @author Yehaifeng
 */
export const compare = function (key) {
    return function (obj1, obj2) {
        let val1 = obj1[key];
        let val2 = obj2[key];
        if (!isNaN(Number(val1)) && !isNaN(Number(val2))) {
            val1 = Number(val1);
            val2 = Number(val2);
        }
        if (val1 < val2) {
            return -1;
        } else if (val1 > val2) {
            return 1;
        } else {
            return 0;
        }
    };
};

/**
 * 随机生成字符串
 * 使用示例 randomString(6) 生成6位的字符串
 * @returns {String}
 * @author Yehaifeng
 */
export const randomString = (e) => {
    var e = e || 32,
        t = 'ABCDEFGHJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789',
        a = t.length,
        n = '';
    for (let i = 0; i < e; i++) n += t.charAt(Math.floor(Math.random() * a));
    return n;
};

/**
 * 防抖函数
 * 多次连续点击时，只执行第一次的点击
 * 使用示例 __debounce(fun,wait)  fun:事件处理函数， wait:延迟时间
 * @returns {String}
 * @author Yehaifeng
 */
export const __debounce = (fun: Function, wait: Number): Function => {
    let timer,
        count = 0; //维护全局纯净，借助闭包来实现
    return function () {
        if (!count) {
            count++;
            fun();
        } else {
            if (timer) {
                clearTimeout(timer);
            }
            timer = setTimeout(function () {
                //timer有值为真，这个事件已经触发了一次，重新开始计数
                count = 0;
            }, wait);
        }
    };
};

/**
 * 防抖函数
 * 多次连续点击时，只执行最后一次的点击
 * 使用示例 debounce__(fun,wait)  fun:事件处理函数， wait:延迟时间
 * @returns {String}
 * @author Yehaifeng
 */
export const debounce__ = (fun: Function, wait: Number): Function => {
    var timer; //维护全局纯净，借助闭包来实现
    return function () {
        if (timer) {
            //timer有值为真，这个事件已经触发了一次，重新开始计数
            clearTimeout(timer);
        }
        timer = setTimeout(function () {
            fun();
        }, wait);
    };
};

/**
 * 防抖函数
 * 使用示例 debounce(fun,wait)  fun:事件处理函数， wait:延迟时间
 * @returns {String}
 * @author Yehaifeng
 */
export const debounce = (fun: Function, wait: number): Function => {
    var timer; //维护全局纯净，借助闭包来实现
    return function () {
        if (timer) {
            //timer有值为真，这个事件已经触发了一次，重新开始计数
            clearTimeout(timer);
        }
        timer = setTimeout(function () {
            fun();
        }, wait);
    };
};

/**字体像素设置函数 */
export function getConcreteSize(fontSize: string, actualValue: number) {
    switch (fontSize) {
        case 'large':
            return Math.floor(actualValue * 1.3);
        case 'small':
            if (Math.floor(actualValue * 0.9) > 12) {
                return Math.floor(actualValue * 0.9);
            } else {
                return 12;
            }
        case 'default':
        case 'medium':
            return Math.floor(actualValue * 1);
        default:
            break;
    }
}

import y9_storage from '@/utils/storage';
// 分页组件，设置记忆性的时候，设置的分页对象的值与存储的值对应
export function getStoragePageSize(uniqueId, defaultNum) {
    let uniquePageSize = y9_storage.getObjectItem('uniquePageSize', uniqueId);
    if (uniquePageSize) {
        return uniquePageSize;
    } else {
        return defaultNum;
    }
}

// 分页组件，设置记忆性的时候，设置的分页对象的值与存储的值对应
export function getStorageCurrPage(uniqueId, defaultNum) {
    let uniqueCurrPage = y9_storage.getObjectItem('uniqueCurrPage', uniqueId);
    if (uniqueCurrPage) {
        return uniqueCurrPage;
    } else {
        return defaultNum;
    }
}

export function base64ImgtoFile(dataurl: any, filename = 'file') {
    const arr = dataurl.split(',');

    const mime = arr[0].match(/:(.*?);/)[1];

    const suffix = mime.split('/')[1];

    const bstr = atob(arr[1]);

    let n = bstr.length;

    const u8arr = new Uint8Array(n);

    while (n--) {
        u8arr[n] = bstr.charCodeAt(n);
    }

    return new File([u8arr], `${filename}.${suffix}`, {
        type: mime
    });
}

export function base64ToUrl(base64) {
    const file = base64ImgtoFile(base64);
    const url = window.webkitURL.createObjectURL(file) || window.URL.createObjectURL(file);
    return url;
}