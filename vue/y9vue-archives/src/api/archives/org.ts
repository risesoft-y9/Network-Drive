import Request from '@/api/lib/request';

var archivesRequest = new Request();

export default {
    getOrganization(){
        var url = "/vue/org/getOrganization";
        return archivesRequest.get(url);
    },
    getTree(id, name) {
        var url = "/vue/org/getTree?id=" + id + "&name=" + name;
        return archivesRequest.get(url);
    },
    getSearchTree(params) {
        var url = "/vue/org/getTree?id=" + params.id + "&name=" + params.name;
        return archivesRequest.get(url);
    },
    getPositionList(){
        var url = "/vue/org/getPositionList";
        return archivesRequest.get(url);
    },
    checkManager(){
        var url = "/vue/org/checkManager";
        return archivesRequest.get(url);
    }
};
