/**
 * Complaint is namespace component for Complaint
 *
 * @author jwu
 */
var Complaint = Complaint || {
    create: function() {
        i18n.init({
            lng: 'en-au',
            lowerCaseLng: true,
            resGetPath: App.getContextPath() + '/resources/i18n/complaint/__lng__.json'
        }, function(){
            $('*[data-i18n]').i18n();

            if (Complaint.Controller.create) {Complaint.Controller.create();}
            if (Complaint.Model.create)      {Complaint.Model.create();}
            if (Complaint.View.create)       {Complaint.View.create();}

            if (ObjNav.create) {
                ObjNav.create({name: "complaint"
                    ,$tree            : Complaint.View.Navigator.$tree
                    ,treeArgs         : Complaint.View.Navigator.getTreeArgs()
                    ,$ulFilter        : Complaint.View.Navigator.$ulFilter
                    ,treeFilter       : Complaint.View.MicroData.treeFilter
                    ,$ulSort          : Complaint.View.Navigator.$ulSort
                    ,treeSort         : Complaint.View.MicroData.treeSort
                    ,modelInterface   : Complaint.Model.interface
                });
            }

            if (DocTree.create) {
                DocTree.create({name: "complaint"
                    ,fileTypes     : Complaint.View.MicroData.fileTypes
                    ,uploadForm    : Complaint.View.Documents.uploadForm
//                ,parentType        : Complaint.Model.DOC_TYPE_COMPLAINT
//                ,parentId          : null
//                ,$tree            : Complaint.View.Documents.$tree
//                ,treeArgs         : Complaint.View.Documents.getTreeArgs()
//                ,getActiveObjId     : ObjNav.View.Navigator.getActiveObjId
//                ,getPreviousObjId   : ObjNav.View.Navigator.getPreviousObjId
//                ,getContextMenu     : Complaint.View.Documents.getContextMenu()
                });
            }

            if (SubscriptionOp.create) {
                SubscriptionOp.create({
                    getSubscriptionInfo: function() {
                        return {userId: App.getUserName()
                            ,objectType: Complaint.Model.DOC_TYPE_COMPLAINT
                            ,objectId: Complaint.Model.getComplaintId()
                        };
                    }
                });
            }
        });
    }

    ,onInitialized: function() {
        if (Complaint.Controller.onInitialized) {Complaint.Controller.onInitialized();}
        if (Complaint.Model.onInitialized)      {Complaint.Model.onInitialized();}
        if (Complaint.View.onInitialized)       {Complaint.View.onInitialized();}

        if (ObjNav.onInitialized)               {ObjNav.onInitialized();}
        if (DocTree.onInitialized)              {DocTree.onInitialized();}
        if (SubscriptionOp.onInitialized)       {SubscriptionOp.onInitialized();}
    }

};

