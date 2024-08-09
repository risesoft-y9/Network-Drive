import Request from '@/api/lib/request';

var storageRequest = new Request();

export default {
    getOrganization(){
        var url = "/vue/org/getOrganization";
        return storageRequest.get(url);
    },
    getTree(id, name) {
        var url = "/vue/org/getTree?id=" + id + "&name=" + name;
        return storageRequest.get(url);
    },
    getSearchTree(params) {
        var url = "/vue/org/getTree?id=" + params.id + "&name=" + params.name;
        return storageRequest.get(url);
    },
    getPositionList(){
        var url = "/vue/org/getPositionList";
        return storageRequest.get(url);
    },
    checkManager(){
        var url = "/vue/org/checkManager";
        return storageRequest.get(url);
    }
};
