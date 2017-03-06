/**
 * https://github.com/nakupanda/bootstrap3-dialog
 */

function showInfo(msg, title) {
    BootstrapDialog.show({
                type: BootstrapDialog.TYPE_INFO,
                title: title ? title : 'Info',
                message: msg,
                buttons: [okButton()]
            });     
}

function showError(msg) {
    BootstrapDialog.show({
                type: BootstrapDialog.TYPE_DANGER,
                title: 'Error',
                message: msg,
                buttons: [okButton()]
            });     
}

function okButton() {
    return {
        id: 'btn-ok',   
        icon: 'glyphicon glyphicon-check',       
        label: 'OK',
        cssClass: 'btn-primary', 
        autospin: false,
        action: function(dialogRef){    
            dialogRef.close();
        }
    };
}