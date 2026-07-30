export async function getLoginInfo() {
    let sessionObj = JSON.parse(sessionStorage.getItem(import.meta.env.VUE_APP_SSO_SITETOKEN_KEY));
    return await fetch(import.meta.env.VUE_APP_SSO_DOMAINURL + 'y9home/api/rest/index/getLoginInfo', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
            Authorization: 'Bearer ' + sessionObj.access_token
        }
    })
        .then((res) => {
            return res.json();
        })
        .then((res) => {
            // console.log(res.data);
            sessionStorage.setItem('getLoginInfo', 'true');
            sessionStorage.setItem('departmentMapList', JSON.stringify(res.data.departmentMapList));
            if (res.data.isShowMenu && res.data.person.tenantManager) {
                // store.commit('user/SET_ROLES', ['admin']);   // AAAAA
            } else if (!res.data.person.tenantManager) {
                // store.commit('user/SET_ROLES', ['user']);    // AAAAA
            } else {
                // store.commit('user/SET_ROLES', ['admin']);   // AAAAA
            }
            // store.commit('user/SET_INITINFO', res.data);     // AAAAA
            return res;
        })
        .catch((e) => {
            console.log(e);
            // sessionStorage.clear();
            // window.location = window.location.origin + window.location.pathname;
            sessionStorage.setItem('getLoginInfo', 'false');
            window.location = window.location.origin + window.location.pathname;
        });
}
