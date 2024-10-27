import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {HttpClientModule} from '@angular/common/http';
import {CommonModule} from '@angular/common';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatDialogModule} from '@angular/material/dialog';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from '@angular/forms';
import {MatInputModule} from "@angular/material/input";
import {MatIconModule} from "@angular/material/icon";
import {TestApiComponent} from './components/test-api/test-api.component';
import {ApiModule, Configuration} from './openApi';
import {HomeComponent} from './components/home/home.component';

export const routes: Routes = [
  {path: '', redirectTo: 'home', pathMatch: 'full'},
  {path: 'home', component: HomeComponent},
  {path: 'api', component: TestApiComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes),
    HttpClientModule,
    CommonModule,
    MatFormFieldModule,
    FormsModule,
    MatDialogModule,
    MatInputModule,
    MatIconModule,
    BrowserModule,
    BrowserAnimationsModule,
    ApiModule.forRoot(() => new Configuration({basePath: 'http://localhost:8080'}))
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
