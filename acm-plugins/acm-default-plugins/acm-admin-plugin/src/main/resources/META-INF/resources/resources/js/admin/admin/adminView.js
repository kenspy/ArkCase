/**
 * Created by manoj.dhungana on 12/4/2014.
 */


Admin.View = Admin.View || {
    create: function() {
        if (Admin.View.AccessControl.create)        {Admin.View.AccessControl.create();}
        if (Admin.View.Correspondence.create)       {Admin.View.Correspondence.create();}
        if (Admin.View.Organization.create)         {Admin.View.Organization.create();}

        if (Admin.View.Tree.create)                 {Admin.View.Tree.create();}
    }
    ,onInitialized: function() {
        if (Admin.View.AccessControl.onInitialized)        {Admin.View.AccessControl.onInitialized();}
        if (Admin.View.Correspondence.onInitialized)       {Admin.View.Correspondence.onInitialized();}
        if (Admin.View.Organization.onInitialized)         {Admin.View.Organization.onInitialized();}

        if (Admin.View.Tree.onInitialized)                 {Admin.View.Tree.onInitialized();}
    }

    ,Organization:{
        create: function () {
            //if (Admin.View.Organization.Tree.create)        {Admin.View.Organization.Tree.create();}
            Acm.Dispatcher.addEventListener(Admin.Controller.MODEL_RETRIEVED_GROUPS, this.onModelRetrievedHierarchy);
            Acm.Dispatcher.addEventListener(Admin.Controller.MODEL_REMOVED_GROUP, this.onModelRetrievedHierarchy);

            if (Admin.View.Organization.Modal.create)         {Admin.View.Organization.Modal.create();}

            /*Acm.Dispatcher.addEventListener(Admin.Controller.MODEL_RETRIEVED_GROUP, this.onModelRetrievedHierarchy);
            Acm.Dispatcher.addEventListener(Admin.Controller.MODEL_RETRIEVED_GROUP_MEMBERS, this.onModelRetrievedHierarchy);*/
        }
        , onInitialized: function () {
            if (Admin.View.Organization.Modal.onInitialized)         {Admin.View.Organization.Modal.onInitialized();}
        }
        ,onModelRetrievedHierarchy: function(){
            if (Admin.View.Organization.Tree.create)        {Admin.View.Organization.Tree.create();}
        }
        ,findSubgroupDetails: function(subgroupName){
            var subgroups = Admin.Model.Organization.cacheSubgroups.get("subgroups");
            var subgroup = {};
            for(var i = 0; i < subgroups.length; i++){
                if(subgroups[i].name == subgroupName){
                    subgroup = subgroups[i];
                    break;
                }
            }
            return subgroup;
        }
        ,findMembers: function(group){
            var members = [];
            if(group.member_id_ss !=null){
                var memberIds = group.member_id_ss;
                for(var j = 0; j< memberIds.length; j++){
                    var member = {};
                    member.title = memberIds[j];
                    /*member.firstName = group.object_sub_type_s;
                    member.lastName = group.supervisor_id_ss;
                    member.jobTitle = group.location;
                    member.groupRole = group.role;*/
                    member.isMember = true;
                    member.folder = false;
                    member.children = [];
                    members.push(member);
                }
            }
            return members;
        }
        ,Modal:{
            create: function () {
                this.$modalCreateAdHocGroup = $("#createAdHoc");
                this.$txtGroupName = $("#groupName");
                this.$adHocGroup = $("#addAdHocGroup");
                this.$adHocGroup.on("click", function(e) {Admin.View.Organization.Modal.onClickBtnCreateAdHocGroup(e, this);});
            }
            , onInitialized: function () {

            }
            ,onClickBtnCreateAdHocGroup:function(event, ctrl){
                event.preventDefault();
                var groupName = Admin.View.Organization.Modal.getTextGroupName();
                if(groupName != null && groupName != ""){
                    var group = {};
                    group.name = groupName;
                    Admin.View.Organization.Modal.hideCreateAdHocGroupModal();
                    Admin.Service.Organization.createAdHocGroup(group);
                }
                else{
                    Acm.Dialog.info("Please enter group name.");
                };
            }

            ,hideCreateAdHocGroupModal: function() {
                this.$modalCreateAdHocGroup.modal('hide');
            }
            ,getTextGroupName: function() {
                return Acm.Object.getValue(this.$txtGroupName);
            }

        }
        ,Tree:{
            create: function () {
                this.$treeOrganization = $("#treeOrganization");
                this.$removeAdHocGroup = $("#removeAdHocGroup");

                this._useFancyTree(this.$treeOrganization);
            }
            , onInitialized: function () {
            }
            ,onClickBtnRemoveAdHocGroup: function(node){
                event.preventDefault();
                if(node.title != "" && node.title != null){
                    if(node.data.isMember == true){
                        var member = {};
                        member.userId = node.title;
                        var parentGroupId = node.parent.title;
                        //data should be sth like this : [{"userId":"ann-acm"}]
                        Admin.Service.Organization.removeGroupMember([member], parentGroupId);
                    }
                    else{
                        var groupId = node.title;
                        Admin.Service.Organization.removeGroup(groupId);
                    }
                }
            }
            ,_useFancyTree: function($s) {
                $s.fancytree({
                    extensions: ["table"],
                    checkbox: false,
                    table: {
                        indentation: 20,      // indent 20px per node level
                        nodeColumnIdx: 2,     // render the node title into the 2nd column
                        checkboxColumnIdx: 0  // render the checkboxes into the 1st column
                    }
                    ,source: function() {
                        var groups = Admin.Model.Organization.cacheGroups.get("groups");
                        var source = [];
                        for(var i = 0; i < groups.length; i++) {
                            var group = {};
                            var subgroups = [];
                            group.title = groups[i].name;
                            group.type = groups[i].object_sub_type_s;
                            group.supervisor = groups[i].supervisor_id_ss;
                            group.location = groups[i].location;
                            group.expanded = true;
                            group.folder = true;
                            //find group members
                            var members = Admin.View.Organization.findMembers(groups[i]);
                            if (members.length > 0) {
                                for (var k = 0; k < members.length; k++) {
                                    subgroups.push(members[k]);
                                }
                            }
                            /*if(groups[i].member_id_ss !=null){
                             var children = groups[i].member_id_ss;
                             for(var j = 0; j< children.length; j++){
                             var child = {};
                             var subgroup = Admin.View.Organization.findSubGroup(children[j]);
                             child.title = children[j];
                             child.firstName = subgroup.object_sub_type_s;
                             child.lastName = subgroup.supervisor_id_ss;
                             child.jobTitle = subgroup.location;
                             child.groupRole = subgroup.role;
                             child.folder = false;
                             child.children = [];
                             subgroups.push(child);
                             }
                             }*/
                            if (groups[i].child_id_ss != null) {
                                var children = groups[i].child_id_ss;
                                for (var j = 0; j < children.length; j++) {
                                    var subGroupM = [];
                                    var child = {};
                                    var subgroup = Admin.View.Organization.findSubgroupDetails(children[j]);
                                    child.title = children[j];
                                    child.type = subgroup.object_sub_type_s;
                                    child.supervisor = subgroup.supervisor_id_ss;
                                    child.location = subgroup.location;
                                    child.folder = true;

                                    //find subgroup members
                                    var subGroupMembers = Admin.View.Organization.findMembers(subgroup);
                                    if (subGroupMembers.length > 0) {
                                        for (var l = 0; l < subGroupMembers.length; l++) {
                                            subGroupM.push(subGroupMembers[l]);
                                        }
                                    }
                                    child.children = subGroupM;
                                    subgroups.push(child);
                                }
                            }
                            group.children = subgroups;
                            source.push(group);
                        }
                        return source;
                    } //end source
                    ,renderColumns: function(event, data) {
                        var groups = Admin.Model.Organization.cacheGroups.get("groups");
                        var node = data.node,
                            $tdList = $(node.tr).find(">td");
                            // (index #0 is rendered by fancytree by adding the checkbox)
                            //$tdList.eq(1).text(node.getIndexHier()).addClass("alignRight");
                            // (index #2 is rendered by fancytree)

                        //right now, users do not have required information, so this code stub is kept here for future purposes
                        //refactoring will be done as the module develops further
                        if(node.title != "Subgroup" && node.title != "Group"){
                            $tdList.eq(3).text(node.data.type);
                            $tdList.eq(4).text(node.data.supervisor);
                            $tdList.eq(5).text(node.data.location);
                            if(node.data.type == "ADHOC_GROUP"){
                                $tdList.eq(6).html("<button class='btn btn-default btn-xs' type='button' id='removeAdHocGroup'><i class='fa fa-trash-o'></i></button>");
                            }
                            //$tdList.eq(6).html("<input type='checkbox' name='like' value='" + node.key + "'>"); <i class='fa fa-chevron-right'></i>
                        }
                        else{
                            $tdList.eq(3).text(node.data.type);
                            $tdList.eq(4).text(node.data.supervisor);
                            $tdList.eq(5).text(node.data.location);
                        }

                            //html("<input type='checkbox' name='like' value='" + node.key + "'>");
                    }
                }); //end fancytree

                $s.delegate("button[id=removeAdHocGroup]", "click", function(e) {
                    var node = $.ui.fancytree.getNode(e),
                        $input = $(e.target);
                    e.stopPropagation();
                    Admin.View.Organization.Tree.onClickBtnRemoveAdHocGroup(node);
                });

                $s.contextmenu({
                    //delegate: "span.fancytree-title",
                    delegate: ".fancytree-title",
                    beforeOpen: function(event, ui) {
                        var node = $.ui.fancytree.getNode(ui.target);
                        node.setActive();
                    },
                    select: function(event, ui) {
                        var node = $.ui.fancytree.getNode(ui.target);
                        alert("select " + ui.cmd + " on " + node);
                    }
                });

            }
        }
    }
    ,AccessControl : {
        create: function () {

            this.$divAdminAccessControlPolicy = $("#divACP");
            this.createJTableAdminAccessControl(this.$divAdminAccessControlPolicy);

            Acm.Dispatcher.addEventListener(Admin.Controller.MODEL_UPDATED_ACCESS_CONTROL, this.onModelUpdatedAccessControlList);

        }
        , onInitialized: function () {
        }


        , onModelUpdatedAccessControlList: function () {
            AcmEx.Object.JTable.load(Admin.View.AccessControl.$divAdminAccessControlPolicy);
        }
        , _makeJtData: function (accessControlList) {
            var jtData = AcmEx.Object.JTable.getEmptyRecords();
            if (accessControlList) {
                for (var i = 0; i < accessControlList.length; i++) {
                    var Record = {};
                    Record.objectType = accessControlList[i].objectType;
                    Record.objectState = accessControlList[i].objectState;
                    Record.accessLevel = accessControlList[i].accessLevel;
                    Record.accessorType = accessControlList[i].accessorType;
                    Record.accessDecision = accessControlList[i].accessDecision;
                    Record.allowDiscretionaryUpdate = (accessControlList[i].allowDiscretionaryUpdate);
                    Record.id = accessControlList[i].id;
                    jtData.Records.push(Record);
                }
                jtData.TotalRecordCount = Admin.Model.getTotalCount();
            }
            return jtData;
        }
        , createJTableAdminAccessControl: function ($jt) {
            var sortMap = {};
            sortMap["dateTime"] = "auditDateTime";

            AcmEx.Object.JTable.usePaging($jt
                , {
                    title: 'Data Access Control'
                    , selecting: true
                    , multiselect: false
                    , selectingCheckboxes: false
                    , actions: {
                        pagingListAction: function (postData, jtParams, sortMap) {
                            //var pageIndex = jtParams.jtStartIndex;
                            var pageIndex = jtParams.jtPageSize.toString() + jtParams.jtStartIndex.toString();
                            if (0 > pageIndex) {
                                return AcmEx.Object.JTable.getEmptyRecords();
                            }
                            var accessControlList = Admin.Model.AccessControl.cacheAccessControlList.get(pageIndex);
                            if (accessControlList) {
                                return Admin.View.AccessControl._makeJtData(accessControlList);

                            } else {
                                return Admin.Service.AccessControl.retrieveAccessControlListDeferred(postData
                                    , jtParams
                                    , sortMap
                                    , function (data) {
                                        var accessControlList = data;
                                        return Admin.View.AccessControl._makeJtData(accessControlList);
                                    }
                                    , function (error) {
                                    }
                                );
                            }  //end else
                        }
                        , updateAction: function (postData, jtParams) {
                            var record = Acm.urlToJson(postData);
                            var rc = {"Result": "OK", "Record": {}};
                            rc = AcmEx.Object.JTable.getEmptyRecord();
                            rc.Record.accessDecision = record.accessDecision;
                            rc.Record.allowDiscretionaryUpdate = record.allowDiscretionaryUpdate;
                            return rc;

//                        return {
//                            "Result": "OK", "Record": { "id": 3, "objectType": "Dr.", "objectState": "Joe", "accessLevel": "Lee", "accessorType": "Witness", "accessDecision": "someone", "allowDiscretionaryUpdate": "dd" }
//                        };
                            //                    var rc = {"Result": "OK", "Record": {id:123, objectType:"hello", objectState:"st", accessLevel: "lv", accessDecision:"ds", allowDiscretionaryUpdate:"ad"}};
                            //                    return rc;
                        }
                    }
                    , fields: {
                        id: {
                            title: 'ID', key: true, type: 'hidden'
                            //   ,list: true
                            , create: false, edit: false
                        }, objectType: {
                            title: 'Object Type', width: '3%', edit: false
                            //,sorting : true
                            , options: [
                                { Value: 'Complaint', DisplayText: 'Complaint' },
                                { Value: 'Task', DisplayText: 'Task' },
                                { Value: 'caseFile', DisplayText: 'Case File' }
                            ]

                        }, objectState: {
                            title: 'State', width: '3%', edit: false, options: [
                                { Value: 'ACTIVE', DisplayText: 'Active' },
                                { Value: 'ASSIGNED', DisplayText: 'Assigned' },
                                { Value: 'COMPLETE', DisplayText: 'Complete' },
                                { Value: 'DRAFT', DisplayText: 'Draft' },
                                { Value: 'IN APPROVAL', DisplayText: 'In Approval' },
                                { Value: 'Scheduled', DisplayText: 'Scheduled' },
                                { Value: 'UNASSIGNED', DisplayText: 'Unassigned' }
                            ]
                        }, accessLevel: {
                            title: 'Access Level', width: '5%', edit: false, options: [
                                { Value: 'Add Document', DisplayText: 'Add Document' },
                                { Value: 'Add Item', DisplayText: 'Add Item' },
                                { Value: 'Approve Complaint', DisplayText: 'Approve Complaint' },
                                { Value: 'delete', DisplayText: 'Delete' },
                                { Value: 'read', DisplayText: 'Read' },
                                { Value: 'Save', DisplayText: 'Save' },
                                { Value: 'Submit for Approval', DisplayText: 'Submit for Approval' },
                                { Value: 'update', DisplayText: 'Update' }
                            ]
                        }, accessorType: {
                            title: 'Accessor Type', width: '5%', edit: false
                        }, accessDecision: {
                            title: 'Access Decision',
                            width: '5%', options: [
                                { Value: 'GRANT', DisplayText: 'Grant' },
                                { Value: 'DENY', DisplayText: 'Deny' },
                                { Value: 'MANDATORY_DENY', DisplayText: 'Mandatory Deny' }
                            ]

                            // ,options: ['GRANT' , 'DENY', 'MANDATORY_DENY']
                        }, allowDiscretionaryUpdate: {
                            title: 'Allow Discretionary',
                            width: '10%', options: [
                                { Value: 'true', DisplayText: 'True' } ,
                                { Value: 'false', DisplayText: 'False' }
                            ]
                        }

                    } //end field
                    ,recordUpdated: function (event, data) { //opened handler
                        var adminAccessUpdated = {};
                        adminAccessUpdated.id = data.record.id;
                        adminAccessUpdated.objectType = data.record.objectType;
                        adminAccessUpdated.objectState = data.record.objectState;
                        adminAccessUpdated.accessLevel = data.record.accessLevel;
                        adminAccessUpdated.accessorType = data.record.accessorType;
                        adminAccessUpdated.accessDecision = data.record.accessDecision;
                        if ("true" == data.record.allowDiscretionaryUpdate) {
                            adminAccessUpdated.allowDiscretionaryUpdate = true;
                        } else {
                            adminAccessUpdated.allowDiscretionaryUpdate = false;
                        }
                        Admin.Service.AccessControl.updateAdminAccess(adminAccessUpdated);
                    }
                } //end arg
                , sortMap
            );
        }
    }
    ,Correspondence : {
        create: function () {

            this.$divCorrespondenceTemplates = $("#divCorrespondenceTemplates");
            this.createJTableCorrespondenceTemplates(this.$divCorrespondenceTemplates);
            this.$btnNewTemplate = $("#addNewTemplate");
            this.$formNewTemplate = $("#formAddNewTemplate");

            this.$btnNewTemplate.on("change", function(e) {Admin.View.Correspondence.onChangeFileInput(e, this);});
            this.$formNewTemplate.submit(function(e) {Admin.View.Correspondence.onSubmitAddTemplate(e, this);});

            AcmEx.Object.JTable.clickAddRecordHandler(this.$divCorrespondenceTemplates,this.onClickSpanAddNewTemplate);

            Acm.Dispatcher.addEventListener(Admin.Controller.MODEL_RETRIEVED_CORRESPONDENCE_TEMPLATES, this.onModelRetrievedCorrespondenceTemplates);

        }
        , onInitialized: function () {
        }
        ,onClickSpanAddNewTemplate: function(event, ctrl) {
            Admin.View.Correspondence.$btnNewTemplate.click();
        }
        ,onChangeFileInput: function(event, ctrl) {
            Admin.View.Correspondence.$formNewTemplate.submit();
        }
        ,onSubmitAddTemplate: function(event, ctrl) {
            event.preventDefault();
            var count = Admin.View.Correspondence.$btnNewTemplate[0].files.length;
            var fd = new FormData();
            for(var i = 0; i < count; i++ ){
                fd.append("files[]", Admin.View.Correspondence.$btnNewTemplate[0].files[i]);
            }
            Admin.Service.Correspondence.uploadTemplateFile(fd);
            Admin.View.Correspondence.$formNewTemplate[0].reset();
        }
        , onModelRetrievedCorrespondenceTemplates: function () {
            AcmEx.Object.JTable.load(Admin.View.Correspondence.$divCorrespondenceTemplates);
        }
        ,createJTableCorrespondenceTemplates: function ($s) {
            $s.jtable({
                title: 'Correspondence Management', messages: {
                    addNewRecord: 'Add New Template'
                }, actions: {
                    listAction: function (postData, jtParams) {
                        var rc = AcmEx.Object.jTableGetEmptyRecords();
                        var templates = Admin.Model.Correspondence.cacheTemplatesList.get(0);
                        if (templates) {
                            for (var i = 0; i < templates.length; i++) {
                                var template = templates[i];
                                var record = {};
                                //record.id = Acm.goodValue(template.id, 0);
                                record.title = Acm.goodValue(template.name);
                                record.created = Acm.getDateFromDatetime(template.created);
                                record.creator = Acm.goodValue(template.creator);
                                record.path = Acm.goodValue(template.path);
                                record.modified = Acm.getDateFromDatetime(template.modified);
                                rc.Records.push(record);
                            }
                        }
                        return rc;
                    }, createAction: function (postData, jtParams) {
                        return {
                            "Result": "OK"
                        };
                    }
                }, fields: {
                    id: {
                        title: 'ID'
                        , key: true
                        , list: false
                        , create: false
                        , edit: false
                    }, title: {
                        title: 'Title'
                        , width: '30%'
                        , display: function (commData) {
                            var a = "<a href='" + App.getContextPath() + Admin.Service.Correspondence.API_DOWNLOAD_TEMPLATE
                                + commData.record.path + "'>" + commData.record.title + "</a>";
                            return $(a);
                        }
                    }, created: {
                        title: 'Created'
                        , width: '15%'
                        , edit: false
                    }, modified: {
                        title: 'Modified'
                        , width: '15%'
                        , edit: false
                    }, creator: {
                        title: 'Creator'
                        , width: '15%'
                        , edit: false
                    }
                }
            });
            $s.jtable('load');
        }
    }

    ,Tree:{
        create: function () {
            this.$btnCreateAdHocGroup = $("#btnCreateAdHoc");
            this.$tree = $("#tree");
            this._useFancyTree(this.$tree);
        }
        , onInitialized: function () {
        }
        ,showPanel: function(key) {
            var tabIds = Admin.Model.Tree.Key.getTabIds();
            var tabIdsToShow = Admin.Model.Tree.Key.getTabIdsByKey(key);
            for (var i = 0; i < tabIds.length; i++) {
                var show = Acm.isItemInArray(tabIds[i], tabIdsToShow);
                Acm.Object.show($("#" + tabIds[i]), show);
                if(show == true && tabIdsToShow == "tOrganization"){
                    this.$btnCreateAdHocGroup.show();
                }
                else{
                    this.$btnCreateAdHocGroup.hide();
                }
            }
        }
        ,_useFancyTree: function($s) {

            $s.fancytree({
                activate: function(event, data){
                    var node = data.node;
                    Admin.View.Tree.showPanel(node.key);
                },
                source: function() {
                    return Admin.View.Tree.treeSource();
                } //end source

            }); //end fancytree

            $s.contextmenu({
                //delegate: "span.fancytree-title",
                delegate: ".fancytree-title",
                beforeOpen: function(event, ui) {
                    var node = $.ui.fancytree.getNode(ui.target);
                    node.setActive();
                },
                select: function(event, ui) {
                    var node = $.ui.fancytree.getNode(ui.target);
                    alert("select " + ui.cmd + " on " + node);
                }
            });

        }

        ,treeSource: function() {
            var builder = AcmEx.FancyTreeBuilder.reset();

            builder.addBranch({key: "acc"                                                   //level 1: /Access Control
                ,title: "Security"
                ,tooltip: "Security"
                ,folder : true
                ,expanded: true
            })
                .addLeaf({key: "dac"                                                        //level 1.1: /Access Control/Data Access Control
                    ,title: "Data Access Control"
                    ,tooltip: "Data Access Control"
                })
                .addLeaf({key: "fac"                                                        //level 1.2: /Access Control/Functional Access Control
                    ,title: "Functional Access Control"
                    ,tooltip: "Functional Access Control"
                })
                .addLeaf({key: "ldap"                                                   //level 1.3: /Access Control/LDAP Configuration
                    ,title: "LDAP Configuration"
                    ,tooltip: "LDAP Configuration"
                })
                .addLeafLast({key: "og"                                                        //level 1.1: /Access Control/Data Access Control
                    ,title: "Organizational Hierarchy"
                    ,tooltip: "Organizational Hierarchy"
                })

            builder.addBranch({key: "dsh"                                               //level 2: /Dashboard
                ,title: "Dashboard"
                ,tooltip: "Dashboard"
                ,folder : true
                ,expanded: true
            })
                .addLeafLast({key: "dc"                                                 //level 2.1: /Dashboard/Dashboard Configuration
                    ,title: "Dashboard Configuration"
                    ,tooltip: "Dashboard Configuration"
                })

            builder.addBranch({key: "rpt"                                               //level 3: /Reports
                ,title: "Reports"
                ,tooltip: "Reports"
                ,folder : true
                ,expanded: true
            })
                .addLeafLast({key: "rc"                                                     //level 3.1: /Reports/Reports Configuration
                    ,title: "Reports Configuration"
                    ,tooltip: "Reports Configuration"
                })


            //for demo purposes
            builder.addBranch({key: "forms"                                               //level 4: /Forms
                ,title: "Forms"
                ,tooltip: "Forms"
                ,folder : true
                ,expanded: true
            })
                .addLeafLast({key: "fc"                                                           //level 4.1: /Forms/Form Configuration
                    ,title: "Form Configuration"
                    ,tooltip: "Form Configuration"
                })
                .addBranch({key: "wf"                                                               //level 4.1.1: /Forms/Form Configuration/Workflows
                    ,title: "Workflows"
                    ,tooltip: "Workflows"
                    ,folder : true
                    ,expanded: true
                })
                .addLeafLast({key: "wfc"                                                                //level 4.1.1.1: /Forms/Form Configuration/Workflows/Workflow Configuration
                    ,title: "Workflow Configuration"
                    ,tooltip: "Workflow Configuration"
                })

                .addBranch({key: "wfl"                                                              //level 4.2.1: /Forms/Form Configuration/Form/Workflow Link
                    ,title: "Form/Workflow Link"
                    ,tooltip: "Form/Workflow Link"
                    ,folder : true
                    ,expanded: true
                })
                .addLeafLast({key: "wfc"                                                                //level 4.2.1.1: /Forms/Form Configuration/Form/Workflow Link/Link Forms/Workflows
                    ,title: "Link Forms/Workflows"
                    ,tooltip: "Link Forms/Workflows"
                })

                .addBranch({key: "bo"                                                               //level 4.3.1: /Forms/Form Configuration/Form/Business Objects
                    ,title: "Business Objects"
                    ,tooltip: "Business Objects"
                    ,folder : true
                    ,expanded: true
                })
                .addLeafLast({key: "wfc"                                                                    //level 4.3.1.1: /Forms/Form Configuration/Form/Business Objects/Business Object Configuration
                    ,title: "Business Object Configuration"
                    ,tooltip: "Business Object Configuration"
                })

                .addBranch({key: "al"                                                           //level 4.4.1: /Forms/Form Configuration/Form/Application Labels
                    ,title: "Application Labels"
                    ,tooltip: "Application Labels"
                    ,folder : true
                    ,expanded: true
                })
                .addLeafLast({key: "lc"                                                                 //level 4.4.1.1: /Forms/Form Configuration/Form/Application Labels/Label Configuration
                    ,title: "Label Configuration"
                    ,tooltip: "Label Configuration"
                })
                .addBranchLast({key: "cm"                                                           //level 4.5.1: /Forms/Form Configuration/Form/Correspondence Management
                    ,title: "Correspondence Management"
                    ,tooltip: "Correspondence Management"
                    ,folder : true
                    ,expanded: true
                })
                .addLeafLast({key: "ct"                                                                 //level 4.5.1.1: /Forms/Form Configuration/Form/Correspondence Templates
                    ,title: "Correspondence Templates"
                    ,tooltip: "Correspondence Templates"
                })

            return builder.getTree();
        }
    }

};
