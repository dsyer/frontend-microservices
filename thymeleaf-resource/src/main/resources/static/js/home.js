angular.module("home", []).controller("home", function($http) {
    var self = this;
    $http.get("resource").then(function(response) {
        self.message = response.data;
    });
});
