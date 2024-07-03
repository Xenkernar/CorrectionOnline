import { defineStore } from "pinia"
import { ref } from "vue"

export const useUserStore = defineStore('user', () => {
    const token = ref('')
    const id = ref('')
    const name = ref('')
    const role = ref('')
    const section = ref('')

    function setName(value) {
        name.value = value
    }
    function login(data) {
        token.value = data.token
        id.value = data.id
        name.value = data.name
        role.value = data.role
        section.value = data.section
    }

    function logout() {
        token.value = ''
        id.value = ''
        name.value = ''
        role.value = ''
        section.value = ''
    }

    function hasFullInfo() {
        return id.value !== '' && name.value !== '' && role.value !== '' && section.value !== ''
    }

    return {
        token,
        id,
        name,
        role,
        section,
        setName,
        login,
        logout,
        hasFullInfo
    }
},{
    persist: {
        storage: sessionStorage,
        paths: ['token', 'id','name', 'role', 'section']
    }
})