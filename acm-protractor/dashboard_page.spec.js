var dashPage = require('./Pages/dashboard_page.js');
var authentication = require('./authentication.js');
var flag = false;


function testAsync(done) {

    setTimeout(function() {
        flag = true;
        done();
    }, 20000);
}

describe('dashboard page test', function() {

    beforeEach(function(done) {

        authentication.loginAsSupervisor();
        testAsync(done);

    });

    afterEach(function() {

        authentication.logout();

    });

    it('should add/delete widget cases by status', function() {

        dashPage.editBtn.click();
        dashPage.addNewWidgetBtn.click().then(function() {
            dashPage.casesByStatus.click();
            dashPage.saveChangesBtn.click();
            expect(dashPage.widgetTitle.getText()).toEqual('Cases By Status');
        });
        dashPage.editBtn.click().then(function() {
            dashPage.removeWidgetBtn.click().then(function() {
                dashPage.saveChangesBtn.click();
            });
        });
    });

    it('should add/delete widget my cases', function() {

        dashPage.editBtn.click();
        dashPage.addNewWidgetBtn.click().then(function() {
            dashPage.myCases.click();
            dashPage.saveChangesBtn.click();
            expect(dashPage.widgetTitle.getText()).toEqual('My Cases');
        });
        dashPage.editBtn.click().then(function() {
            dashPage.removeWidgetBtn.click().then(function() {
                dashPage.saveChangesBtn.click();
            });
        });

    });

    it('should add/delete widget my complaints', function() {

        dashPage.editBtn.click();
        dashPage.addNewWidgetBtn.click().then(function() {
            dashPage.myComplaints.click();
            dashPage.saveChangesBtn.click();
            expect(dashPage.widgetTitle.getText()).toEqual('My Complaints');
        });
        dashPage.editBtn.click().then(function() {
            dashPage.removeWidgetBtn.click().then(function() {
                dashPage.saveChangesBtn.click();
            });
        });
    });


    it('should add/delete widget new complaints', function() {

        dashPage.editBtn.click();
        dashPage.addNewWidgetBtn.click().then(function() {
            dashPage.newComplaints.click();
            dashPage.saveChangesBtn.click();
            expect(dashPage.widgetTitle.getText()).toEqual('New Complaints');
        });
        dashPage.editBtn.click().then(function() {
            dashPage.removeWidgetBtn.click().then(function() {
                dashPage.saveChangesBtn.click();
            });
        });

    });


    it('should add/delete widget team workload', function() {

        dashPage.editBtn.click();
        dashPage.addNewWidgetBtn.click().then(function() {
            dashPage.teamWorkload.click();
            dashPage.saveChangesBtn.click();
            expect(dashPage.widgetTitle.getText()).toEqual('Team Workload');
        });
        dashPage.editBtn.click().then(function() {
            dashPage.removeWidgetBtn.click().then(function() {
                dashPage.saveChangesBtn.click();
            });
        });
    });

    it('should add/delete widget weather', function() {

        dashPage.editBtn.click();
        dashPage.addNewWidgetBtn.click().then(function() {
            dashPage.weather.click();
            dashPage.saveChangesBtn.click();
            expect(dashPage.widgetTitle.getText()).toEqual('Weather');
        });
        dashPage.editBtn.click().then(function() {
            dashPage.removeWidgetBtn.click().then(function() {
                dashPage.saveChangesBtn.click();
            });
        });
    });

    it('should add/delete widget news', function() {

        dashPage.editBtn.click();
        dashPage.addNewWidgetBtn.click().then(function() {
            dashPage.news.click();
            dashPage.saveChangesBtn.click();
            expect(dashPage.widgetTitle.getText()).toEqual('News');
        });
        dashPage.editBtn.click().then(function() {
            dashPage.removeWidgetBtn.click().then(function() {
                dashPage.saveChangesBtn.click();
            });

        });
    });
});
