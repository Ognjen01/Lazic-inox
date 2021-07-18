package com.ognjenlazic.lazicinox

import android.Manifest
import com.google.ar.sceneform.ux.ArFragment

class CustomArFragment : ArFragment(){

    override fun getAdditionalPermissions(): Array<String> {
        val additionalPermission = super.getAdditionalPermissions()
        val permissionsLenght = additionalPermission.size
        val permissions = Array(permissionsLenght + 1) {
            Manifest.permission.WRITE_EXTERNAL_STORAGE}
            if (permissionsLenght > 0) {
                System.arraycopy(additionalPermission, 0, permissions, 1, permissionsLenght)

        }
        return permissions
    }
}