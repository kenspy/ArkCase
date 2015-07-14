/**
 * AcmNotification is namespace component for AcmNotification plugin
 *
 * ('Notification' is taken, so we settle with 'AcmNotification')
 *
 * @author jwu
 */
var AcmNotification = AcmNotification || {
    create: function() {
        if (AcmNotification.Controller.create) {AcmNotification.Controller.create();}
        if (AcmNotification.Model.create)      {AcmNotification.Model.create();}
        if (AcmNotification.View.create)       {AcmNotification.View.create();}

        if (SearchBase.create) {
            SearchBase.create({name: AcmNotification.Model.MicroData.searchName
                ,jtArgs     : AcmNotification.View.getJtArgs()
                //,jtDataMaker: AcmNotification.View.jtDataMaker
                ,filters    : AcmNotification.Model.MicroData.getSearchFilters()
            });
        }
    }

    ,onInitialized: function() {
        if (AcmNotification.Controller.onInitialized) {AcmNotification.Controller.onInitialized();}
        if (AcmNotification.Model.onInitialized)      {AcmNotification.Model.onInitialized();}
        if (AcmNotification.View.onInitialized)       {AcmNotification.View.onInitialized();}

        if (SearchBase.onInitialized)        {SearchBase.onInitialized();}
    }
};
