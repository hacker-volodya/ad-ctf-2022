import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {DropletsComponent} from './droplets/droplets.component';
import {FormsModule} from "@angular/forms";
import {DropletDetailComponent} from './droplet-detail/droplet-detail.component';
import {HttpClientModule} from "@angular/common/http";
import { DropletCreateComponent } from './droplet-create/droplet-create.component';
import { AppRoutingModule } from './app-routing.module';
import {MatListModule} from "@angular/material/list";
import {MatIconModule} from "@angular/material/icon";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatButtonModule} from "@angular/material/button";

@NgModule({
  declarations: [
    AppComponent,
    DropletsComponent,
    DropletDetailComponent,
    DropletCreateComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    HttpClientModule,
    AppRoutingModule,
    MatListModule,
    MatIconModule,
    MatToolbarModule,
    MatButtonModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
